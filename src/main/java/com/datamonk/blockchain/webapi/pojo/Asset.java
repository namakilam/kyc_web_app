package com.datamonk.blockchain.webapi.pojo;

import com.datamonk.blockchain.webapi.enums.AssetType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by namakilam on 11/08/17.
 */
public class Asset implements Serializable {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String id;

    @JsonProperty("type")
    private AssetType assetType;

    @JsonProperty("area")
    private float area;

    @JsonProperty("metric")
    private String metric;

    @JsonProperty("owner")
    private String owner;

    private Asset(Builder builder) {
        this.id = builder.id;
        this.assetType = builder.assetType;
        this.area = builder.area;
        this.metric = builder.metric;
        this.owner = builder.owner;
    }

    public static Builder newAsset() {
        return new Builder();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public static final class Builder {
        private String id;
        private AssetType assetType;
        private float area;
        private String metric;
        private String owner;

        private Builder() {
        }

        public Asset build() {
            return new Asset(this);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder assetType(AssetType assetType) {
            this.assetType = assetType;
            return this;
        }

        public Builder area(float area) {
            this.area = area;
            return this;
        }

        public Builder metric(String metric) {
            this.metric = metric;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }
    }

    public static Asset toAsset(String value) throws IOException {
        return objectMapper.readValue(value, Asset.class);
    }

    public static String toJsonString(Asset asset) throws IOException {
        return objectMapper.writeValueAsString(asset);
    }
}
