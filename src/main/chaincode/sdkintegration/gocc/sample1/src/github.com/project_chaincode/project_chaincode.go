package main

import (
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"encoding/json"
)

type MilestoneStatusUpdateRequest struct {
	MilestoneId string `json:"milestone_id"`
	NewStatus bool `json:"new_status"`
	ApprovalStatus string `json:"approval_status"`
	Responder string `json:"responder"`
}

type OwnerUpdateRequest struct {
	NewOwner string `json:"new_owner"`
	Accepted bool `json:"accepted"`
}

type Milestone struct {
	Id string `json:"id"`
	MilestoneValue string `json:"milestone_value"`
	PerMilestone float32 `json:"per_milestone"`
	Status bool `json:"status"`
}

type Project struct {
	Id	string `json:"id"`
	Activity string `json:"activity"`
	Weightage float32 `json:"weightage"`
	SubWeightage float32 `json:"sub_weightage"`
	Milestones []Milestone `json:"milestone"`
	MilestoneValue int `json:"milestone_value"`
	Children []string `json:"children"`
	Parent string `json:"parent"`
	StatusUpdateRequest	[]MilestoneStatusUpdateRequest `json:"status_update_request"`
	OwnerTransferRequest OwnerUpdateRequest `json:"owner_transfer_request"`
	Owner string `json:"owner"`
	PreviousOwner string `json:"previous_owner"`
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
	if len(project.Owner) == 0 {
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
	case "getProjectByOwner":
		return t.getProjectByOwner(stub, args[1:])
	case "projectOwnerTransferRequest":
		return t.projectOwnerTransferRequest(stub, args[1:])
	case "approveProjectOwnerTransferRequest":
		return t.approveProjectOwnerTransferRequest(stub, args[1:])
	default:
		fmt.Println("Unknown Function Call")
		return shim.Error("Unknown Function Call")
	}
}

func (t *ProjectChaincode) readHistoryFromLedger(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect Number of Arguments. Required : 1")
	}

	key := args[0]
	historyItr, err := stub.GetHistoryForKey(key)

	if err != nil {
		return shim.Error(err.Error())
	}

	var history []string

	for historyItr.HasNext() {
		alters, err := historyItr.Next()
		if err != nil {
			fmt.Println(err)
		} else {
			history = append(history, string(alters.Value))
		}
	}

	val, err := json.Marshal(history)
	if err != nil {
		return shim.Error(err.Error())
	}
	return shim.Success(val)
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

func (t *ProjectChaincode) getProjectByOwner(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	indexName := "OwnerProjectKey"

	queryItr, err := stub.GetStateByPartialCompositeKey(indexName, []string{args[0]})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	var values []Project

	for queryItr.HasNext() {
		res, err := queryItr.Next()

		if err != nil {
			fmt.Errorf(err.Error())
		} else {
			var asset Project
			err = json.Unmarshal(res.Value, &asset)
			if err == nil {
				values = append(values, asset)
			}
		}
	}

	val, err := json.Marshal(values)
	if err != nil {
		return shim.Error(err.Error())
	}

	if len(val) == 0 {
		return shim.Error(fmt.Sprintf("No Object With Prefix-Key : %s present In the Ledger.", args[0]))
	}
	return shim.Success(val)
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
		indexName := "OwnerProjectKey"
		resp := t.getProjectById(stub, []string{project.Id})

		if resp.Status == 200 {
			return shim.Error("Asset Already Present")
		}

		key, err := stub.CreateCompositeKey(indexName, []string{project.Owner, project.Id})

		value, err := json.Marshal(project)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		err = stub.PutState(key, value)

		if err != nil {
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

	indexName := "OwnerProjectKey"
	var project Project
	err := json.Unmarshal(resp.Payload, &project)

	key, err := stub.CreateCompositeKey(indexName, []string{project.Owner, project.Id})

	if err != nil {
		return shim.Error(err.Error())
	}

	err = stub.DelState(project.Id)
	err = stub.DelState(key)

	if err == nil {
		if len(project.Parent) != 0 {
			resp := t.removeTaskFromProject(stub, []string{project.Parent, projectId})
			if resp.Status != 200 {
				value, _:= json.Marshal(project)
				stub.PutState(project.Id, value)
				return shim.Error("Error Deleting Project")
			} else {
				if len(project.Children) > 0 {
					for _, childId := range project.Children {
						t.deleteProjectTask(stub, []string{childId})
					}
				}

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
	if len(args) != 3 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]
	milestoneId := args[1]
	requester := args[2]

	resp := t.getProjectById(stub, []string{projectId})

	if resp.Status != 200 {
		return shim.Error("Project Does not exist")
	}

	var statusUpdateRequest MilestoneStatusUpdateRequest
	statusUpdateRequest.MilestoneId = milestoneId
	statusUpdateRequest.ApprovalStatus = "Waiting"
	statusUpdateRequest.NewStatus = true
	statusUpdateRequest.Responder = ""

	var project Project
	err := json.Unmarshal(resp.Payload, &project)

	if requester != project.Owner {
		return shim.Error("Requesting Party is not the Owner of the Task")
	}

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}
	if &(project.StatusUpdateRequest) == nil {
		project.StatusUpdateRequest = make([]MilestoneStatusUpdateRequest, 0)
	}

	project.StatusUpdateRequest = append(project.StatusUpdateRequest, statusUpdateRequest)

	value, err := json.Marshal(project)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	indexName := "OwnerProjectKey"
	key, err := stub.CreateCompositeKey(indexName, []string{project.Owner, project.Id})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(key, value)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	key, err = stub.CreateCompositeKey(indexName, []string{project.PreviousOwner, project.Id})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(key, value)

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
	if len(args) != 3 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]
	milestoneId := args[1]
	responder := args[2]

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

	if project.PreviousOwner != responder {
		return shim.Error("Invalid Authorization Personnel")
	}

	if &(project.StatusUpdateRequest) == nil {
		fmt.Errorf("No Status Update Request Found")
		return shim.Error("No Status Update Request Found")
	} else {
		var statusUpdateRequest MilestoneStatusUpdateRequest
		var milestone Milestone

		for _, ms := range project.Milestones {
			if ms.Id == milestoneId {
				milestone = ms
				ms.Status = statusUpdateRequest.NewStatus
			}
		}

		for _, updateRequest := range project.StatusUpdateRequest {
			if updateRequest.MilestoneId == milestoneId {
				statusUpdateRequest = updateRequest
				updateRequest.Responder = responder
				updateRequest.ApprovalStatus = "Approved"
				break
			}
		}

		if (&statusUpdateRequest == nil) || (&milestone == nil) {
			return shim.Error("Status Update Request Not Found")
		}

		value , err := json.Marshal(project)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		indexName := "OwnerProjectKey"
		key, err := stub.CreateCompositeKey(indexName, []string{project.Owner, project.Id})

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		err = stub.PutState(key, value)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		key, err = stub.CreateCompositeKey(indexName, []string{project.PreviousOwner, project.Id})

		err = stub.PutState(key, value)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		err = stub.PutState(project.Id, value)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		return shim.Success([]byte("Status Update Approved Successfully"))
	}
}

func (t *ProjectChaincode) declineProjectStatusUpdate(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
	if len(args) != 3 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]
	milestoneId := args[1]
	responder := args[2]

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

	if project.PreviousOwner != responder {
		return shim.Error("Invalid Authorization Personnel")
	}

	if &(project.StatusUpdateRequest) == nil {
		fmt.Errorf("No Status Update Request Found")
		return shim.Error("No Status Update Request Found")
	} else {
		var statusUpdateRequest MilestoneStatusUpdateRequest

		for _, updateRequest := range project.StatusUpdateRequest {
			if updateRequest.MilestoneId == milestoneId {
				statusUpdateRequest = updateRequest
				break
			}
		}

		if &statusUpdateRequest == nil {
			return shim.Error("Status Update Request Not Found")
		}

		statusUpdateRequest.Responder = responder
		statusUpdateRequest.ApprovalStatus = "Declined"

		value , err := json.Marshal(project)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		indexName := "OwnerProjectKey"
		key, err := stub.CreateCompositeKey(indexName, []string{project.Owner, project.Id})

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		err = stub.PutState(key, value)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		key, err = stub.CreateCompositeKey(indexName, []string{project.PreviousOwner, project.Id})

		err = stub.PutState(key, value)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		stub.PutState(project.Id, value)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		return shim.Success([]byte("Status Update Declined Successfully"))
	}
}
func (t *ProjectChaincode) projectOwnerTransferRequest(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 3 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]
	requester := args[1]
	newOwner := args[2]

	response := t.getProjectById(stub, []string{projectId})

	if response.Status != 200 {
		return shim.Error("Property Doesn't Exist")
	}

	var asset Project
	err := json.Unmarshal(response.Payload, &asset)

	if err != nil {
		return shim.Error(err.Error())
	}

	if asset.Owner != requester {
		return shim.Error("Not Authorized to Transfer Ownership")
	}

	var ownerUpdateReqesut OwnerUpdateRequest
	ownerUpdateReqesut.Accepted = false
	ownerUpdateReqesut.NewOwner = newOwner

	asset.OwnerTransferRequest = ownerUpdateReqesut

	indexName := "OwnerProjectKey"
	key, err := stub.CreateCompositeKey(indexName, []string{asset.Owner, projectId})

	if err != nil {
		return shim.Error(err.Error())
	}

	value, err := json.Marshal(asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(key, value)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	key, err = stub.CreateCompositeKey(indexName, []string{newOwner, projectId})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(key, value)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(projectId, value)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}
	return shim.Success([]byte("Transfer Request Successfully Created"))
}


func (t *ProjectChaincode) approveProjectOwnerTransferRequest(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	projectId := args[0]
	requester := args[1]

	response := t.getProjectById(stub, []string{projectId})

	if response.Status != 200 {
		return shim.Error("Property Doesn't Exist")
	}

	var asset Project
	err := json.Unmarshal(response.Payload, &asset)

	if err != nil {
		return shim.Error(err.Error())
	}

	if &(asset.OwnerTransferRequest) == nil {
		return shim.Error("No Owner Transfer Request Present")
	}

	if asset.OwnerTransferRequest.NewOwner != requester {
		return shim.Error("Not Authorized to Accept Request")
	}

	asset.PreviousOwner = asset.Owner
	asset.Owner = asset.OwnerTransferRequest.NewOwner

	asset.OwnerTransferRequest.NewOwner = ""
	asset.OwnerTransferRequest.Accepted = false

	indexName := "OwnerProjectKey"
	key, err := stub.CreateCompositeKey(indexName, []string{asset.PreviousOwner, projectId})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	value, err := json.Marshal(asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(key, value)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	key, err = stub.CreateCompositeKey(indexName, []string{asset.Owner, projectId})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(key, value)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(asset.Id, value)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("Ownership Transfered Successfully"))
}

func main() {
	err := shim.Start(new(ProjectChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}