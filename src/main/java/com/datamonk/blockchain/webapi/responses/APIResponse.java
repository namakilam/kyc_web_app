package com.datamonk.blockchain.webapi.responses;

import com.datamonk.blockchain.webapi.requests.APIRequest;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by namakilam on 05/08/17.
 */
public class APIResponse {
    private static final String DEFAULT_JSON_RPC = "2.0";
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("jsonrpc")
    private String jsonRpc;

    @JsonProperty("result")
    private Map<String, Object> resultMap;

    private APIResponse(Builder builder) {
        this.id = builder.id;
        this.jsonRpc = builder.jsonRpc;
        this.resultMap = builder.resultMap;
    }

    public static Builder newAPIResponse() {
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

    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }


    public static final class Builder {
        private Integer id;
        private String jsonRpc;
        private Map<String, Object> resultMap;

        private Builder() {
        }

        public APIResponse build() {
            return new APIResponse(this);
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder jsonRpc(String jsonRpc) {
            this.jsonRpc = jsonRpc;
            return this;
        }

        public Builder resultMap(Map<String, Object> resultMap) {
            this.resultMap = resultMap;
            return this;
        }
    }

    public static APIResponse Success(APIRequest request, String message) {
        return APIResponse.newAPIResponse().id(request.getId())
                .jsonRpc(request.getJsonRpc())
                .resultMap(new HashMap<String, Object>() {
                    {
                        put("status", 200);
                        put("message", message);
                    }
                }).build();
    }

    public static APIResponse Success(APIRequest request, Map<String, Object> resultMap) {
        resultMap.put("status", 200);
        return APIResponse.newAPIResponse().id(request.getId())
                .jsonRpc(request.getJsonRpc())
                .resultMap(resultMap).build();

    }

    public static APIResponse Success(Map<String, Object> resultMap) {
        resultMap.put("status", 200);
        return APIResponse.newAPIResponse().id(1)
                .jsonRpc(DEFAULT_JSON_RPC)
                .resultMap(resultMap).build();
    }

    public static APIResponse Failure(Throwable e) {
        return APIResponse.newAPIResponse().id(1)
                .jsonRpc(DEFAULT_JSON_RPC)
                .resultMap(new HashMap<String, Object>() {
                    {
                        put("status", 400);
                        put("message", e.getMessage());
                    }
                }).build();
    }

    public static APIResponse Failure(APIRequest request, Throwable e) {
        return APIResponse.newAPIResponse().id(request.getId())
                .jsonRpc(request.getJsonRpc())
                .resultMap(new HashMap<String, Object>() {
                    {
                        put("status", 400);
                        put("message", e.getMessage());
                    }
                }).build();
    }
}
