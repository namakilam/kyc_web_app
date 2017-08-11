package com.datamonk.blockchain.webapi.requests;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hyperledger.fabric.sdk.ChaincodeID;

import java.util.Map;

/**
 * Created by namakilam on 05/08/17.
 */
public class RequestParams {
    @JsonProperty("type")
    private Integer type;

    @JsonProperty("chaincodeID")
    private Map<String, Object> chaincodeID;

    @JsonProperty("secureContext")
    private String secureContext;

    @JsonProperty("CtorMsg")
    private CtorMsg ctorMsg;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Map<String, Object> getChaincodeID() {
        return chaincodeID;
    }

    public void setChaincodeID(Map<String, Object> chaincodeID) {
        this.chaincodeID = chaincodeID;
    }

    public String getSecureContext() {
        return secureContext;
    }

    public void setSecureContext(String secureContext) {
        this.secureContext = secureContext;
    }

    public CtorMsg getCtorMsg() {
        return ctorMsg;
    }

    public void setCtorMsg(CtorMsg ctorMsg) {
        this.ctorMsg = ctorMsg;
    }
}
