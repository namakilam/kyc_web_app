package main

import (
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"encoding/json"
)

type Address struct {
	addressLine string `json:"address_line"`
	city string `json:"city"`
}

type PropertyTransferRequest struct {
	newOwnerId string `json:"new_owner_id"`
	accepted bool `json:"accepted"`
}

type Asset struct {
	id string`json:"id"`
	asset_type string `json:"type"`
	area float32 `json:"area"`
	metric string `json:"metric"`
	owner string `json:"owner"`
	propertyTransferRequest PropertyTransferRequest `json:"property_transfer_request"`
}

type PropertyChaincode struct {
}

func (t *PropertyChaincode) validate(asset Asset) bool {
	return &asset.id != nil &&
		(asset.asset_type == "property" || asset.asset_type == "land") &&
		&asset.area != nil &&
		&asset.metric != nil &&
		&asset.owner != nil &&
		len(asset.owner) == 12
}

func (t *PropertyChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	return shim.Success([]byte("I.N.I.T SUCCESS"))
}

func (t *PropertyChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response  {
	fmt.Print("####    PROPERTY CHAINCODE INVOKED    ####")
	function, args := stub.GetFunctionAndParameters()

	if function != "invoke" {
		return shim.Error("Unknown Function Call")
	}

	if len(args) < 2 {
		return shim.Error("Incorrect Number of Arguments. Expecting atleast 2")
	}

	switch args[0] {
	case "insert":
		return t.insertNewProperty(stub, args)
	case "getById":
		return t.getPropertyById(stub, args)
	case "getByOwner":
		return t.getPropertyByOwner(stub, args)
	case "transferRequest":
		return t.transferPropertyRequest(stub, args)
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

	key, err := stub.CreateCompositeKey(indexName, []string{asset.owner, propertyId})

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

	if &(asset.propertyTransferRequest) == nil {
		return shim.Error("No Transfer Request Found")
	}

	if asset.propertyTransferRequest.accepted == false {
		return shim.Error("Transfer Request Not Accepted By New Owner")
	}

	asset.owner = asset.propertyTransferRequest.newOwnerId
	asset.propertyTransferRequest.newOwnerId = ""
	asset.propertyTransferRequest.accepted = false

	newKey, err := stub.CreateCompositeKey(indexName, []string{asset.owner, propertyId})

	value,err := json.Marshal(asset)

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
}

func (t *PropertyChaincode) acceptTransferRequest(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 3 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	newOwnerId := args[0]
	propertyId := args[1]

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

	key, err := stub.CreateCompositeKey(indexName, []string{asset.owner, propertyId})

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
		return shim.Error(err.Error())
	}

	if &(asset.propertyTransferRequest) == nil {
		return shim.Error("No Transfer Request Found")
	}

	if asset.propertyTransferRequest.newOwnerId != newOwnerId {
		return shim.Error("Invalid New Owner Id Supplied")
	}

	asset.propertyTransferRequest.accepted = true

	value,err := json.Marshal(asset)

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

func (t *PropertyChaincode) transferPropertyRequest(stub shim.ChaincodeStubInterface, args []string) pb.Response  {
	if len(args) != 4 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]
	propertyId := args[0]
	ownerId := args[1]

	response := t.getPropertyById(stub, []string{"getById", propertyId})

	if response.Status != 200 {
		return shim.Error("Property Doesn't Exist")
	}

	var asset Asset
	err := json.Unmarshal(response.Payload, &asset)

	if err != nil {
		return shim.Error(err.Error())
	}

	if asset.owner != ownerId {
		jsonError := "Requesting User Not the owner of the property"
		return shim.Error(jsonError)
	}


	var transferRequest PropertyTransferRequest
	transferRequest.newOwnerId = args[2]
	transferRequest.accepted = false

	asset.propertyTransferRequest = transferRequest
	indexName := "compositePropertykey"

	key, err := stub.CreateCompositeKey(indexName, []string{ownerId, propertyId})

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error("Error Creating Composite Key")
	}

	value,err := json.Marshal(asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

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

	var values []string

	for queryItr.HasNext() {
		res, err := queryItr.Next()

		if err != nil {
			fmt.Errorf(err.Error())
		} else {
			values = append(values, string(res.Value))
		}
	}

	val, err := json.Marshal(values)
	if err != nil {
		return shim.Error(err.Error())
	}
	return shim.Success(val)
}



func (t *PropertyChaincode) insertNewProperty(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 2 {
		return shim.Error("Incorrect Number of Arguments.")
	}

	args = args[1:]

	var asset Asset
	err := json.Unmarshal([]byte(args[0]), asset)

	if err != nil {
		fmt.Errorf(err.Error())
		return shim.Error(err.Error())
	}

	if t.validate(asset) {
		indexName := "compositePropertykey"
		response := t.getPropertyById(stub, []string{"getById", asset.id})

		if response.Status == 200 {
			return shim.Error("Asset Already Present")
		}

		key, err := stub.CreateCompositeKey(indexName, []string{asset.owner, asset.id})

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error("Error Creating Composite Key")
		}

		value,err := json.Marshal(asset)

		if err != nil {
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		err = stub.PutState(key, value)

		if err != nil {
			return shim.Error(err.Error())
		}

		err = stub.PutState(asset.id, value)
		if err != nil {
			stub.DelState(key)
			if err != nil {
				fmt.Errorf(err.Error())
				return shim.Error(err.Error())
			}
			fmt.Errorf(err.Error())
			return shim.Error(err.Error())
		}

		fmt.Printf("Insert Success for %s", asset.id)
		return shim.Success([]byte("Insert Success"))
	} else {
		return shim.Error("Asset Structure Not Valid. Please check JSON.")
	}
}

func main() {
	err := shim.Start(new(PropertyChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}