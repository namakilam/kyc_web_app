package com.datamonk.blockchain.webapi;

import com.datamonk.blockchain.hyperledger.ChainService;
import com.datamonk.blockchain.webapi.requests.APIRequest;
import com.datamonk.blockchain.webapi.responses.APIResponse;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Created by namakilam on 04/08/17.
 */
@Path("kyc")
public class API {
    private ChainService chainService;

    public API(ChainService chainService) {
        this.chainService = chainService;
    }

    @GET
    @Path("health")
    @Produces({MediaType.APPLICATION_JSON})
    public String checkHealth() {
        return "Health O.K";
    }


    @POST
    @Path("chaincode")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public APIResponse handleChaincodeRequest(APIRequest request) {
        if (APIRequest.validate(request)) {
            switch (request.getRequestParams().getCtorMsg().getFunction()) {
                case "insert":
                    return insertDataIntoLedger(request);
                case "retrieve":
                    return retrieveDataFromLedger(request);
                case "history":
                    return historyDataFromLedger(request);
                case "historyProperty":
                    return historyDataFromLedgerProperty(request);
                case "update":
                    return updateDataFromLedger(request);
                case "insertProperty":
                    return insertPropertyIntoLedger(request);
                case "getPropertyById":
                    return getPropertyById(request);
                case "getPropertyInfoByOwner":
                    return getPropertyByOwner(request);
                case "propertyTransferRequest":
                    return transferPropertyRequest(request);
                case "acceptPropertyTransferRequest":
                    return acceptPropertyTransferRequest(request);
                case "approvePropertyTransfer":
                    return approvePropertyTransfer(request);
                case "getBlockByNumber":
                    return getBlockByNumber(request);
                case "insertProject":
                    return insertProjectIntoLedger(request);
                case "updateProjectStatus":
                    return updateProjectStatus(request);
                case "deleteProject":
                    return deleteProjectFromLedger(request);
                case "getProjectById":
                    return getProjectById(request);
                case "approveProjectTaskUpdate":
                    return approveProjectTaskUpdate(request);
                case "declineProjectTaskUpdate":
                    return declineProjectTaskUpdate(request);
            }
        }
        return APIResponse.Failure(new BadRequestException());
    }

    @GET
    @Path("queryChain")
    @Produces({MediaType.APPLICATION_JSON})
    public APIResponse queryChannelForChaincode() {
        try {
            Map<String, Object> resultMap = chainService.queryChannelForChaincodes();
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }


    private APIResponse retrieveDataFromLedger(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.queryDataFromLedger(request);
            return APIResponse.Success(request, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse insertDataIntoLedger(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.insertIntoLedger(request);
            return APIResponse.Success(request, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse historyDataFromLedger(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.historyDataFromLedger(request);
            return APIResponse.Success(request, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse historyDataFromLedgerProperty(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.historyDataFromLedgerProperty(request);
            return APIResponse.Success(request, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse updateDataFromLedger(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.updateDataIntoLedger(request);
            return APIResponse.Success(request, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse approvePropertyTransfer(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.approveTransferRequest(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse acceptPropertyTransferRequest(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.acceptPropertyTransferRequest(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse transferPropertyRequest(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.propertyTransferRequest(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse getPropertyByOwner(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.getPropertyInfoByOwner(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse getPropertyById(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.getPropertyInfoById(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse insertPropertyIntoLedger(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.insertPropertyIntoLedger(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse getBlockByNumber(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.getBlockByNumber(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse insertProjectIntoLedger(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.insertProjectIntoLedger(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse updateProjectStatus(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.updateProjectStatus(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse deleteProjectFromLedger(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.deleteProjectFromLedger(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse getProjectById(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.getProjectInfoById(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse approveProjectTaskUpdate(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.approveProjectTaskUpdate(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }

    private APIResponse declineProjectTaskUpdate(APIRequest request) {
        try {
            Map<String, Object> resultMap = chainService.declineProjectTaskUpdate(request);
            return APIResponse.Success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.Failure(e);
        }
    }
}
