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
    private String area;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("address")
    private Address address;

    public Asset() {

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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    private Asset(Builder builder) {
        this.id = builder.id;
        this.assetType = builder.assetType;
        this.area = builder.area;
        this.owner = builder.owner;
        this.address = builder.address;
    }


    public static Asset toAsset(String value) throws IOException {
        return objectMapper.readValue(value, Asset.class);
    }

    public static String toJsonString(Asset asset) throws IOException {
        return objectMapper.writeValueAsString(asset);
    }

    public static Builder newAsset() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private AssetType assetType;
        private String area;
        private String owner;
        private Address address;

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

        public Builder area(String area) {
            this.area = area;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }
    }

    private class Address implements Serializable {
        @JsonProperty("address_line")
        private String addressLine;

        @JsonProperty("city")
        private String city;

        public Address() {

        }

        public String getAddressLine() {
            return addressLine;
        }

        public void setAddressLine(String addressLine) {
            this.addressLine = addressLine;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }
}
