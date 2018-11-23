package main

import (
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"encoding/json"
)

type ProjectStatusUpdateRequest struct {
	NewStatus bool `json:"new_status"`
	ApprovalStatus string `json:"approval_status"`
	Responder string `json:"responder"`
}

type Project struct {
	Id	string `json:"id"`
	Activity string `json:"activity"`
	Weightage float32 `json:"weightage"`
	SubWeightage float32 `json:"sub_weightage"`
	PerMilestone float32 `json:"per_milestone"`
	Milestone string `json:"milestone"`
	MilestoneValue int `json:"milestone_value"`
	Status bool `json:"status"`
	Children []string `json:"children"`
	Parent string `json:"parent"`
	StatusUpdateRequest	ProjectStatusUpdateRequest `json:"status_update_request"`
}

type ProjectChaincode struct {
}

func (t *ProjectChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	return shim.Success([]byte("I.N.I.T SUCCESS"))
}

func (t *ProjectChaincode) validateProject(project Project) bool {
	if len(project.Id) == 0 {
		return false
	}
	if project.PerMilestone > 100.0 {
		return false
	}
	return true
}

func (t *ProjectChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	fmt.Println("###    PROJECT CHAINCODE INVOKED    ###")
	function, args := stub.GetFunctionAndParameters()

	if function != "invoke" {
		return shim.Error("Unknown Function Call")
	}

	if len(args) < 2 {
		return shim.Error("Incorrect Number of Arguments. Expecting atleast 2")
	}

	fmt.Println(args[0] + " Invoked")

	switch args[0] {
	case "insert":
		return t.insertProjectTask(stub, args[1:])
	case "delete":
		return t.deleteProjectTask(stub, args[1:])
	case "update":
		return t.updateProjectStatus(stub, args[1:])
	case "approveProjectTaskUpdate":
		return t.approveProjectStatusUpdate(stub, args[1:])
	case "declineProjectTaskUpdate":
		return t.declineProjectStatusUpdate(stub, args[1:])
	case "getProjectById":
		return t.getProjectById(stub, args[1:])
	default:
		fmt.Println("Unknown Function Call")
		return shim.Error("Unknown Function Call")
	}
}

func (t *ProjectChaincode) removeTaskFromProject(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	parentId := args[0]
	childId := args[1]

	resp := t.getProjectById(stub, []string{parentId})

	if resp.Status != 200 {
		return shim.Error("Parent Not Found")
	}

	var parent Project
	err := json.Unmarshal(resp.Payload, &parent)

	if err != nil {
		return shim.Error(err.Error())
	}

	if len(parent.Children) == 0 {
		return shim.Success([]byte("Child Already Deleted"))
	}

	removeIndex := -1
	for id, child := range parent.Children {
		if child == childId {
			removeIndex = id
			break
		}
	}

	if removeIndex != -1 {
		parent.Children[removeIndex] = parent.Children[len(parent.Children)-1]
		parent.Children = parent.Children[:len(parent.Children)-1]

		value, err := json.Marshal(parent)

		if err != nil {
			return shim.Error(err.Error())
		}

		err = stub.PutState(parent.Id, value)

		if err != nil {
			return shim.Error(err.Error())
		}
		return shim.Success([]byte("Child Deleted Successfully"))
	} else {
		return shim.Success([]byte("Child Already Deleted"))
	}
}

func (t *ProjectChaincode) addTaskToProject(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	parentId := args[0]
	childId := args[1]

	resp := t.getProjectById(stub, []string{parentId})

	if resp.Status != 200 {
		return shim.Error("Parent Not Found")
	}

	var parent Project
	err := json.Unmarshal(resp.Payload, &parent)

	if err != nil {
		return shim.Error(err.Error())
	}

	if len(parent.Children) == 0 {
		parent.Children = make([]string, 0)
	}

	parent.Children = append(parent.Children, childId)


	value, err := json.Marshal(parent)
	if err != nil {
		return shim.Error(err.Error())
	}

	stub.PutState(parent.Id, value)

	return shim.Success(value)
}

func (t *ProjectChaincode) getProjectById(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	key := args[0]

	projectInfo, err := stub.GetState(key)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	if len(projectInfo) == 0 {
		return shim.Error(fmt.Sprintf("No Object With Key : %s present In the Ledger.", key))
	}
	return shim.Success(projectInfo)
}

func (t *ProjectChaincode) insertProjectTask(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	fmt.Println(args[0])
	var project Project
	err := json.Unmarshal([]byte(args[0]), &project)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	if t.validateProject(project) {
		resp := t.getProjectById(stub, []string{project.Id})

		if resp.Status == 200 {
			return shim.Error("Asset Already Present")
		}

		value, err := json.Marshal(project)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		err = stub.PutState(project.Id, value)

		if err != nil {
			return shim.Error(err.Error())
		}

		if len(project.Parent) != 0 {
			resp = t.addTaskToProject(stub, []string{project.Parent, project.Id})
		}

		return shim.Success([]byte("Task Added Successfully"))
	} else {
		return shim.Error("Invalid Payload for Project")
	}
}

func (t *ProjectChaincode) deleteProjectTask(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]

	resp := t.getProjectById(stub, []string{projectId})

	if resp.Status != 200 {
		return shim.Error("Project Not Present")
	}

	var project Project
	err := json.Unmarshal(resp.Payload, &project)

	if err != nil {
		return shim.Error(err.Error())
	}

	err = stub.DelState(project.Id)

	if err != nil {
		if len(project.Parent) != 0 {
			resp := t.removeTaskFromProject(stub, []string{project.Parent, projectId})
			if resp.Status != 200 {
				value, _:= json.Marshal(project)
				stub.PutState(project.Id, value)
				return shim.Error("Error Deleting Project")
			} else {
				return shim.Success([]byte("Project Deleted Successfully"))
			}
		} else {
			return shim.Success([]byte("Project Deleted Successfully"))
		}
	} else {
		return shim.Error(err.Error())
	}
}

func (t *ProjectChaincode) updateProjectStatus(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]

	resp := t.getProjectById(stub, []string{projectId})

	if resp.Status != 200 {
		return shim.Error("Project Does not exist")
	}

	var statusUpdateRequest ProjectStatusUpdateRequest
	statusUpdateRequest.ApprovalStatus = "Waiting"
	statusUpdateRequest.NewStatus = true
	statusUpdateRequest.Responder = ""

	var project Project
	err := json.Unmarshal(resp.Payload, &project)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	project.StatusUpdateRequest = statusUpdateRequest

	value, err := json.Marshal(project)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(project.Id, value)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("Status Update Request Created Successfully"))
}

func (t *ProjectChaincode) approveProjectStatusUpdate(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]
	responder := args[1]

	resp := t.getProjectById(stub, []string{projectId})

	if resp.Status != 200 {
		return shim.Error("Project Does not exist")
	}

	var project Project
	err := json.Unmarshal(resp.Payload, &project)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	if &(project.StatusUpdateRequest) == nil {
		fmt.Errorf("No Status Update Request Found")
		return shim.Error("No Status Update Request Found")
	} else {
		project.StatusUpdateRequest.Responder = responder
		project.StatusUpdateRequest.ApprovalStatus = "Approved"
		project.Status = project.StatusUpdateRequest.NewStatus

		value , err := json.Marshal(project)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		stub.PutState(project.Id, value)

		return shim.Success([]byte("Status Update Approved Successfully"))
	}
}

func (t *ProjectChaincode) declineProjectStatusUpdate(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]
	responder := args[1]

	resp := t.getProjectById(stub, []string{projectId})

	if resp.Status != 200 {
		return shim.Error("Project Does not exist")
	}

	var project Project
	err := json.Unmarshal(resp.Payload, &project)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	if &(project.StatusUpdateRequest) == nil {
		fmt.Errorf("No Status Update Request Found")
		return shim.Error("No Status Update Request Found")
	} else {
		project.StatusUpdateRequest.Responder = responder
		project.StatusUpdateRequest.ApprovalStatus = "Declined"

		value , err := json.Marshal(project)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		stub.PutState(project.Id, value)

		return shim.Success([]byte("Status Update Declined Successfully"))
	}
}

func main() {
	err := shim.Start(new(ProjectChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}