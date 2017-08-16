package com.datamonk.blockchain.webapi;

import com.datamonk.blockchain.hyperledger.ChainService;
import com.datamonk.blockchain.hyperledger.exceptions.InconsistentProposalResponseException;
import com.datamonk.blockchain.hyperledger.exceptions.InvalidNumberArgumentException;
import com.datamonk.blockchain.hyperledger.exceptions.NotEnoughEndorsersException;
import com.datamonk.blockchain.webapi.requests.APIRequest;
import com.datamonk.blockchain.webapi.responses.APIResponse;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
}
