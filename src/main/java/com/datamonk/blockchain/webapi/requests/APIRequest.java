package com.datamonk.blockchain.webapi.requests;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 * Created by namakilam on 05/08/17.
 */
public class APIRequest {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("jsonrpc")
    private String jsonRpc;

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private RequestParams requestParams;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJsonRpc() {
        return jsonRpc;
    }

    public void setJsonRpc(String jsonRpc) {
        this.jsonRpc = jsonRpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(RequestParams requestParams) {
        this.requestParams = requestParams;
    }

    public static Boolean validate(APIRequest request) {
        return request != null &&
                request.getRequestParams() != null &&
                request.getRequestParams().getChaincodeID() != null &&
                request.getRequestParams().getCtorMsg() != null &&
                request.getRequestParams().getCtorMsg().getFunction() != null &&
                request.getRequestParams().getCtorMsg().getArgs() != null;
    }
}
