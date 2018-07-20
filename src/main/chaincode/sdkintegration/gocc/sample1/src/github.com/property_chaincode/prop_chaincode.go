package main

import (
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"encoding/json"
	"strings"
	"strconv"
)

type Address struct {
	Address_Line string `json:"address_line"`
	City         string `json:"city"`
}

type Approver struct {
	Id         string `json:"id"`
	Department string `json:"department"`
}

type PropertyTransferRequest struct {
	NewOwnerId    string `json:"new_owner_id"`
	Accepted      bool `json:"accepted"`
	SplitSize     int `json:"split_size"`
	Authorization []Approver `json:"authorizers"`
}

type Asset struct {
	Id                      string`json:"id"`
	Type                    string `json:"type"`
	Area                    int `json:"area"`
	Address                 Address `json:"address"`
	Owner                   string `json:"owner"`
	PropertyTransferRequest PropertyTransferRequest `json:"property_transfer_request"`
	Parent                  string `json:"parentId"`
	Children                []string `json:"children"`
	RegisteredBy            Approver `json:"registeredBy"`
	ApprovedBy              []Approver `json:"approvedBy"`
}

type PropertyChaincode struct {
}

func (t *PropertyChaincode) validate(asset Asset) bool {
	if len(asset.RegisteredBy.Id) == 0 || len(asset.RegisteredBy.Department) == 0 {
		return false
	}
	return true
}

func (t *PropertyChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	return shim.Success([]byte("I.N.I.T SUCCESS"))
}

func (t *PropertyChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	fmt.Println("####    PROPERTY CHAINCODE INVOKED    ####")
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
		return t.insertNewProperty(stub, args)
	case "getById":
		return t.getPropertyById(stub, args)
	case "getByOwner":
		return t.getPropertyByOwner(stub, args)
	case "transferRequest":
		return t.transferPropertyRequest(stub, args)
	case "transferRequestByPart":
		return t.transferPropertyRequestByPart(stub, args)
	case "acceptTransferRequest":
		return t.acceptTransferRequest(stub, args)
	case "approveTransferRequest":
		return t.approveTransferRequest(stub, args)
	default:
		fmt.Print("Unkown Function Call")
		return shim.Error("Unkown Function Call")
	}

	return shim.Error("Unknown action, check the first argument")
}

func (t *PropertyChaincode) approveTransferRequest(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	propertyId := args[0]

	response := t.getPropertyById(stub, []string{"getById", propertyId})

	if response.Status != 200 {
		return shim.Error("Property Doesn't Exist")
	}

	var asset Asset
	err := json.Unmarshal(response.Payload, &asset)

	if err != nil {
		fmt.Printf(err.Error())
		return shim.Error(err.Error())
	}

	indexName := "compositePropertykey"

	key, err := stub.CreateCompositeKey(indexName, []string{asset.Owner, propertyId})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error("Error Creating Composite Key")
	}

	assetVal, err := stub.GetState(key)

	if err != nil {
		fmt.Printf(err.Error())
		return shim.Error(err.Error())
	}

	err = json.Unmarshal(assetVal, &asset)

	if err != nil {
		fmt.Printf(err.Error())
		return shim.Error(err.Error())
	}

	if &(asset.PropertyTransferRequest) == nil {
		return shim.Error("No Transfer Request Found")
	}

	if asset.PropertyTransferRequest.Accepted == false {
		return shim.Error("Transfer Request Not Accepted By New Owner")
	}



	/**
		Old Logic Without the Split Property Logic
	 */
	if asset.PropertyTransferRequest.SplitSize == 0 {
		asset.Owner = asset.PropertyTransferRequest.NewOwnerId
		asset.PropertyTransferRequest.NewOwnerId = ""
		asset.PropertyTransferRequest.Accepted = false
		asset.ApprovedBy = asset.PropertyTransferRequest.Authorization
		asset.PropertyTransferRequest.Authorization = make([]Approver, 0)

		newKey, err := stub.CreateCompositeKey(indexName, []string{asset.Owner, propertyId})

		value, err := json.Marshal(asset)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		err = stub.PutState(newKey, value)

		if err != nil {
			fmt.Printf(err.Error())
			return shim.Error("Could Not Approve Transfer")
		}

		err = stub.DelState(key)

		if err != nil {
			err = stub.DelState(newKey)
			if err != nil {
				fmt.Printf(err.Error())
				return shim.Error("Could Not Approve Transfer")
			}
			fmt.Printf(err.Error())
			return shim.Error("Could Not Approve Transfer")
		}

		err = stub.PutState(propertyId, value)

		if err != nil {
			fmt.Printf(err.Error())
			stub.DelState(newKey)
			stub.PutState(key, value)

			if err != nil {
				return shim.Error("Could Not Approve Transfer")
			}
		}

		return shim.Success([]byte("Property Transfer Sucess"))
	} else {
		/**
			New Logic Of Splitting Property
		 */

		asset1 := asset
		asset2 := asset

		asset1.Id = strings.Join([]string{asset1.Id, "1"}, "/")
		asset2.Id = strings.Join([]string{asset2.Id, "2"}, "/")

		asset2.Area = asset.PropertyTransferRequest.SplitSize
		asset1.Area -= asset2.Area

		asset2.Owner = asset.PropertyTransferRequest.NewOwnerId

		asset1.Parent = asset.Id
		asset2.Parent = asset.Id

		asset.Children = []string{asset1.Id, asset2.Id}

		asset1.PropertyTransferRequest.NewOwnerId = ""
		asset1.PropertyTransferRequest.Accepted = false
		asset1.PropertyTransferRequest.SplitSize = 0
		asset1.ApprovedBy = asset1.PropertyTransferRequest.Authorization
		asset1.PropertyTransferRequest.Authorization = make([]Approver, 0)

		asset2.PropertyTransferRequest.NewOwnerId = ""
		asset2.PropertyTransferRequest.Accepted = false
		asset2.PropertyTransferRequest.SplitSize = 0
		asset2.ApprovedBy = asset1.PropertyTransferRequest.Authorization
		asset2.PropertyTransferRequest.Authorization = make([]Approver, 0)

		err = stub.DelState(key)

		if err != nil {
			return shim.Error("Could Not Approve Transfer")
		}

		newKey, err := stub.CreateCompositeKey(indexName, []string{asset1.Owner, asset1.Id})

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		value, err := json.Marshal(asset1)

		stub.PutState(asset1.Id, value)

		if err != nil {
			return shim.Error("Could Not Approve Transfer")
		}

		err = stub.PutState(newKey, value)

		if err != nil {
			return shim.Error("Could Not Approve Transfer")
		}

		newKey2, err := stub.CreateCompositeKey(indexName, []string{asset2.Owner, asset2.Id})

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		value2, err := json.Marshal(asset2)

		stub.PutState(asset2.Id, value2)

		if err != nil {
			stub.DelState(newKey)
			stub.DelState(asset1.Id)
			return shim.Error("Could Not Approve Transfer")
		}

		err = stub.PutState(newKey2, value2)

		if err != nil {
			stub.DelState(newKey)
			stub.DelState(asset1.Id)
			return shim.Error("Could Not Approve Transfer")
		}

		value, err = json.Marshal(asset)

		err = stub.PutState(asset.Id, value)

		if err != nil {
			stub.DelState(newKey2)
			stub.DelState(asset2.Id)
			stub.DelState(newKey)
			stub.DelState(asset1.Id)
			return shim.Error("Could Not Approve Transfer")
		}
		return shim.Success([]byte("Property Transfer Sucess"))
	}
}

func (t *PropertyChaincode) acceptTransferRequest(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 4 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	newOwnerId := args[0]
	propertyId := args[1]
	approvedBy := args[2]

	var approver Approver
	err := json.Unmarshal([]byte(approvedBy), &approver)

	if err != nil {
		return shim.Error("Unable to Unmarshal the approver")
	}

	response := t.getPropertyById(stub, []string{"getById", propertyId})

	if response.Status != 200 {
		return shim.Error("Property Doesn't Exist")
	}

	var asset Asset
	err = json.Unmarshal(response.Payload, &asset)

	if err != nil {
		fmt.Printf(err.Error())
		return shim.Error(err.Error())
	}

	indexName := "compositePropertykey"

	key, err := stub.CreateCompositeKey(indexName, []string{asset.Owner, propertyId})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error("Error Creating Composite Key")
	}

	assetVal, err := stub.GetState(key)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = json.Unmarshal(assetVal, &asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	if &(asset.PropertyTransferRequest) == nil {
		fmt.Errorf("No Transfer Request Found")
		return shim.Error("No Transfer Request Found")
	}

	if asset.PropertyTransferRequest.NewOwnerId != newOwnerId {
		fmt.Errorf("Invalid New Owner Id Supplied")
		return shim.Error("Invalid New Owner Id Supplied")
	}

	asset.PropertyTransferRequest.Accepted = true
	asset.PropertyTransferRequest.Authorization = append(asset.PropertyTransferRequest.Authorization, approver)

	value, err := json.Marshal(asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	err = stub.PutState(key, value)

	if err != nil {
		fmt.Printf(err.Error())
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("Transfer Request Successfully Accepted"))
}

func (t *PropertyChaincode) transferPropertyRequestByPart(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 6 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	propertyId := args[0]
	ownerId := args[1]
	newOwnerId := args[2]
	splitSize := args[3]
	approvedBy := args[4]

	var approver Approver
	err := json.Unmarshal([]byte(approvedBy), &approver)

	if err != nil {
		return shim.Error("Unable to Unmarshal the approver")
	}

	response := t.getPropertyById(stub, []string{"getById", propertyId})

	if response.Status != 200 {
		return shim.Error("Property Doesn't Exist")
	}

	var asset Asset
	err = json.Unmarshal(response.Payload, &asset)

	if err != nil {
		return shim.Error(err.Error())
	}

	if len(asset.Children) != 0 {
		jsonError := "Spent Property Cannot be spent again"
		return shim.Error(jsonError)
	}

	if asset.Owner != ownerId {
		return shim.Error("Requesting Owner not the Owner of the property")
	}

	propArea := asset.Area
	splitArea, err := strconv.Atoi(splitSize)

	if err != nil {
		return shim.Error("Invalid Split Size")
	}

	if propArea < splitArea {
		return shim.Error("Split Size cannot be greater than property size")
	} else if propArea == splitArea {
		return t.transferPropertyRequest(stub, []string{"transferRequest", propertyId, ownerId, newOwnerId})
	}

	var transferPropertyRequest PropertyTransferRequest
	transferPropertyRequest.NewOwnerId = newOwnerId
	transferPropertyRequest.Accepted = false
	transferPropertyRequest.SplitSize = splitArea
	transferPropertyRequest.Authorization = append(transferPropertyRequest.Authorization, approver)

	asset.PropertyTransferRequest = transferPropertyRequest
	indexName := "compositePropertykey"

	key, err := stub.CreateCompositeKey(indexName, []string{ownerId, propertyId})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error("Error Creating Composite Key")
	}

	value, err := json.Marshal(asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	fmt.Print("Writing Value to Ledger : ")
	fmt.Print(string(value))

	err = stub.PutState(key, value)

	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("Transfer Request Successfully Created"))
}

func (t *PropertyChaincode) transferPropertyRequest(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 5 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	propertyId := args[0]
	ownerId := args[1]
	approvedBy := args[3]

	var approver Approver
	err := json.Unmarshal([]byte(approvedBy), &approver)

	if err != nil {
		return shim.Error("Unable to Unmarshal the approver")
	}

	response := t.getPropertyById(stub, []string{"getById", propertyId})

	if response.Status != 200 {
		return shim.Error("Property Doesn't Exist")
	}

	var asset Asset
	err = json.Unmarshal(response.Payload, &asset)

	if err != nil {
		return shim.Error(err.Error())
	}

	if len(asset.Children) != 0 {
		jsonError := "Spent Property Cannot be spent again"
		return shim.Error(jsonError)
	}

	if asset.Owner != ownerId {
		jsonError := "Requesting User Not the owner of the property"
		return shim.Error(jsonError)
	}

	var transferRequest PropertyTransferRequest
	transferRequest.NewOwnerId = args[2]
	transferRequest.Accepted = false
	transferRequest.SplitSize = 0
	transferRequest.Authorization = append(transferRequest.Authorization, approver)

	asset.PropertyTransferRequest = transferRequest
	indexName := "compositePropertykey"

	key, err := stub.CreateCompositeKey(indexName, []string{ownerId, propertyId})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error("Error Creating Composite Key")
	}

	value, err := json.Marshal(asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	fmt.Print("Writing Value To Ledger : ")
	fmt.Print(string(value))
	err = stub.PutState(key, value)

	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success([]byte("Transfer Request Successfully Created"))
}

func (t *PropertyChaincode) getPropertyById(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	key := args[0]

	assetInfo, err := stub.GetState(key)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	if len(assetInfo) == 0 {
		return shim.Error(fmt.Sprintf("No Object With Key : %s present In the Ledger.", key))
	}
	return shim.Success(assetInfo)
}

func (t *PropertyChaincode) getPropertyByOwner(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	indexName := "compositePropertykey"

	queryItr, err := stub.GetStateByPartialCompositeKey(indexName, []string{args[0]})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	var values []Asset

	for queryItr.HasNext() {
		res, err := queryItr.Next()

		if err != nil {
			fmt.Errorf(err.Error())
		} else {
			var asset Asset
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

func (t *PropertyChaincode) insertNewProperty(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	fmt.Println(args[0])
	var asset Asset
	err := json.Unmarshal([]byte(args[0]), &asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	if t.validate(asset) {
		indexName := "compositePropertykey"
		response := t.getPropertyById(stub, []string{"getById", asset.Id})

		if response.Status == 200 {
			return shim.Error("Asset Already Present")
		}

		if len(asset.Parent) != 0 && len(asset.Children) != 0 {

			return shim.Error("Parent and Children Not Allowed While Inserting Property")
		}

		key, err := stub.CreateCompositeKey(indexName, []string{asset.Owner, asset.Id})

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error("Error Creating Composite Key")
		}

		value, err := json.Marshal(asset)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		err = stub.PutState(key, value)

		if err != nil {
			return shim.Error(err.Error())
		}

		err = stub.PutState(asset.Id, value)
		if err != nil {
			stub.DelState(key)
			if err != nil {
				fmt.Errorf(err.Error())
				return shim.Error(err.Error())
			}
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}
		fmt.Printf("Insert Success for %s", asset.Id)
		return shim.Success([]byte("Insert Success"))
	} else {
		fmt.Println(fmt.Sprintf("Asset Structure Not Valid. Please check JSON. %s", args[0]))
		return shim.Error("Asset Structure Not Valid. Please check JSON.")
	}
}

func main() {
	err := shim.Start(new(PropertyChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}