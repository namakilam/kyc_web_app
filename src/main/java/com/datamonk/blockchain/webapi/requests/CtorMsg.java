package com.datamonk.blockchain.webapi.requests;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Created by namakilam on 05/08/17.
 */
public class CtorMsg {
    @JsonProperty("Function")
    private String function;

    @JsonProperty("Args")
    private List<String> args;

    public CtorMsg() {

    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}
