package main

import (
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
	"encoding/json"
)

type Address struct {
	Address_Line string `json:"address_line"`
	City string `json:"city"`
}

type Customer struct {
	Name string `json:"name"`
	Gender string `json:"gender"`
	DOB string `json:"dob"`
	Aadhar string `json:"aadhar_no"`
	Address Address `json:"address"`
	PAN string `json:"pan_no"`
	Cibil_Score int32 `json:"cibil_score"`
	Marital_Status string `json:"marital_status"`
	Education map[string]string `json:"education"`
	Employement map[string]string `json:"employement"`
	Health map[string]string `json:"health"`
	Possesions map[string]string `json:"possesions"`
}

type SimpleChainCode struct {
}

func main()  {
	err := shim.Start(new(SimpleChainCode))
	if err != nil {
		fmt.Printf("Error starting simple chaincode due to :%s", err)
	}
}

func (t *SimpleChainCode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	fmt.Print("Initializing ChainCode.....")

	return shim.Success([]byte("INITIALIZATION O.K."))
}

func (t *SimpleChainCode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
        function, args := stub.GetFunctionAndParameters()
	
	switch function {
	case "insert":
		return t.insertDataIntoLedger(stub, args)
	case "update":
	    	return t.updateDataIntoLedger(stub, args)
	case "retrieve":
	    	return t.readDataFromLedger(stub, args)
	case "history":
	    	return t.readHistoryFromLedger(stub, args)
	}

	return shim.Error("Unknown Function Invocation")
}

func (t *SimpleChainCode) Query(stub shim.ChaincodeStubInterface, function string, args []string) pb.Response {
	switch function {
	case "retrieve":
		return t.readDataFromLedger(stub, args)
	case "history":
	    	return t.readHistoryFromLedger(stub, args)
	}

	return shim.Error("Unknown Function Invocation")
}

func (t *SimpleChainCode) updateDataIntoLedger(stub shim.ChaincodeStubInterface, args[] string) pb.Response {
    if len(args) != 2 {
	return shim.Error("Incorrect Number of Arguments. Required : 2")
    }

    key := args[0]
    customerInfo, err := stub.GetState(key)

    if err != nil {
	return shim.Error("Entry for given key not found!. Please insert into the ledger first.")
    }
    
    var customer, customer1 Customer
    err = json.Unmarshal(customerInfo, &customer)
    err = json.Unmarshal([]byte(args[1]), &customer1) 
   
    if err != nil {
	return shim.Error("Unable to parse Customer String. Please ensure a valid JSON.")
    }

    if customer.Aadhar == customer1.Aadhar && customer.PAN == customer1.PAN {
	value, err := json.Marshal(customer1)
	if err != nil {
	    return shim.Error(err.Error())
	}

	err = stub.PutState(key, value)
	if err != nil {
	    return shim.Error(err.Error())
	}
	return shim.Success([]byte("Update Successful"))
    } else {
	return shim.Error("Cannot Update Immutable Fields (AADHAR NUMBER, PAN)")
    }
}

func (t *SimpleChainCode) readHistoryFromLedger(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    if len(args) != 1 {
	return shim.Error("Incorrect Number of Arguments. Required : 2")
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
	   history =  append(history, string(alters.Value))
	}
    }
   
    val, err := json.Marshal(history)
    if err != nil {
	return shim.Error(err.Error())
    }
    return shim.Success(val)
}

func (t *SimpleChainCode) readDataFromLedger(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 1 {
	    return shim.Error("Incorrect Number of Arguments. Required : 1")
	}

	customerInfo, err := stub.GetState(args[0])
	if err != nil {
	    return shim.Error(err.Error())
	}

	return shim.Success(customerInfo)
}

func (t * SimpleChainCode) insertDataIntoLedger(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    if len(args) != 1 {
	return shim.Error("Incorrect Number of Arguments. Required : 1") 
    }

    var customer Customer
    err := json.Unmarshal([]byte(args[0]), &customer)
    if err != nil {
	return shim.Error(err.Error())
    }

    key := customer.Aadhar
    value, err := json.Marshal(customer)
    if err != nil {
	return shim.Error(err.Error())
    }
    err = stub.PutState(key, value) 
    if err != nil {
	return shim.Error(err.Error())
    }

    fmt.Println("Ledger state successfully updated")

    return shim.Success([]byte("Insert Success"))
}
