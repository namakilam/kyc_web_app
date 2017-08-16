package com.datamonk.blockchain.webapi.requests;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
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

    public APIRequest() {

    }

    private APIRequest(Builder builder) {
        this.id = builder.id;
        this.jsonRpc = builder.jsonRpc;
        this.method = builder.method;
        this.requestParams = builder.requestParams;
    }

    public static Builder newAPIRequest() {
        return new Builder();
    }

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

    public static final class Builder {
        private Integer id;
        private String jsonRpc;
        private String method;
        private RequestParams requestParams;

        private Builder() {
        }

        public APIRequest build() {
            return new APIRequest(this);
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder jsonRpc(String jsonRpc) {
            this.jsonRpc = jsonRpc;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder requestParams(RequestParams requestParams) {
            this.requestParams = requestParams;
            return this;
        }
    }

    public static APIRequest buildNewRequest(List<String> args) {
        CtorMsg ctorMsg = new CtorMsg();
        ctorMsg.setArgs(args);
        ctorMsg.setFunction("invoke");

        RequestParams requestParams = new RequestParams();
        requestParams.setCtorMsg(ctorMsg);

        APIRequest request = APIRequest.newAPIRequest()
                .id(1)
                .jsonRpc("2.0")
                .method("invoke")
                .requestParams(requestParams)
                .build();

        return request;
    }
}
