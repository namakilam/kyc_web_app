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

    @JsonProperty("parentId")
    private String parent;

    @JsonProperty("children")
    private String children;

    public Asset() {

    }

    private Asset(Builder builder) {
        setId(builder.id);
        setAssetType(builder.assetType);
        setArea(builder.area);
        setOwner(builder.owner);
        setAddress(builder.address);
        setParent(builder.parent);
        setChildren(builder.children);
    }

    public static Builder newBuilder() {
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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getChildren() {
        return children;
    }

    public void setChildren(String children) {
        this.children = children;
    }


    public static Asset toAsset(String value) throws IOException {
        return objectMapper.readValue(value, Asset.class);
    }

    public static String toJsonString(Asset asset) throws IOException {
        return objectMapper.writeValueAsString(asset);
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

    public static final class Builder {
        private String id;
        private AssetType assetType;
        private String area;
        private String owner;
        private Address address;
        private String parent;
        private String children;

        private Builder() {
        }

        public Builder id(String val) {
            id = val;
            return this;
        }

        public Builder assetType(AssetType val) {
            assetType = val;
            return this;
        }

        public Builder area(String val) {
            area = val;
            return this;
        }

        public Builder owner(String val) {
            owner = val;
            return this;
        }

        public Builder address(Address val) {
            address = val;
            return this;
        }

        public Builder parent(String val) {
            parent = val;
            return this;
        }

        public Builder children(String val) {
            children = val;
            return this;
        }

        public Asset build() {
            return new Asset(this);
        }
    }
}
