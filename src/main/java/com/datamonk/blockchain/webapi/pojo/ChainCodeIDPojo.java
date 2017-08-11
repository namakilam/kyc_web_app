package com.datamonk.blockchain.webapi.pojo;

import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.ChaincodeID;

/**
 * Created by namakilam on 05/08/17.
 */
public class ChainCodeIDPojo {
    private String name;
    private String version;
    private String path;
    private String input;
    private String vscc;
    private String escc;

    private ChainCodeIDPojo(Builder builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.path = builder.path;
        this.input = builder.input;
        this.vscc = builder.vscc;
        this.escc = builder.escc;
    }

    public static Builder newChainCodeIDPojo() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getVscc() {
        return vscc;
    }

    public void setVscc(String vscc) {
        this.vscc = vscc;
    }

    public String getEscc() {
        return escc;
    }

    public void setEscc(String escc) {
        this.escc = escc;
    }

    public static final class Builder {
        private String name;
        private String version;
        private String path;
        private String input;
        private String vscc;
        private String escc;

        private Builder() {
        }

        public ChainCodeIDPojo build() {
            return new ChainCodeIDPojo(this);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder input(String input) {
            this.input = input;
            return this;
        }

        public Builder vscc(String vscc) {
            this.vscc = vscc;
            return this;
        }

        public Builder escc(String escc) {
            this.escc = escc;
            return this;
        }
    }

    public static ChainCodeIDPojo convertChaincodeInfoToChaincodeIDPojo(Query.ChaincodeInfo chaincodeInfo) {
        return ChainCodeIDPojo.newChainCodeIDPojo().name(chaincodeInfo.getName())
                .path(chaincodeInfo.getPath())
                .version(chaincodeInfo.getVersion())
                .escc(chaincodeInfo.getEscc())
                .vscc(chaincodeInfo.getVscc())
                .build();
    }
}
