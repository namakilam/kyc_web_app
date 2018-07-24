package com.datamonk.blockchain.webapi.pojo;

import com.datamonk.blockchain.webapi.enums.AssetType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by namakilam on 11/08/17.
 */

public class Asset implements Serializable {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String id;

    @JsonProperty("type")
    private AssetType assetType;

    @JsonProperty("length")
    private Integer length;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("parentId")
    private String parent;

    @JsonProperty("children")
    private List<String> children;

    @JsonProperty("registeredBy")
    private Approver registeredBy;

    public Asset() {

    }

    private Asset(Builder builder) {
        setId(builder.id);
        setAssetType(builder.assetType);
        setLength(builder.length);
        setWidth(builder.width);
        setOwner(builder.owner);
        setAddress(builder.address);
        setParent(builder.parent);
        setChildren(builder.children);
        setRegisteredBy(builder.registeredBy);
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

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
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

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public Approver getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(Approver registeredBy) {
        this.registeredBy = registeredBy;
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

    private class Approver implements Serializable {
        @JsonProperty("id")
        private String id;

        @JsonProperty("department")
        private String department;

        public Approver() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }


    public static final class Builder {
        private String id;
        private AssetType assetType;
        private Integer length;
        private Integer width;
        private String owner;
        private Address address;
        private String parent;
        private List<String> children;
        private Approver registeredBy;

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

        public Builder length(Integer val) {
            length = val;
            return this;
        }

        public Builder width(Integer val) {
            width = val;
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

        public Builder children(List<String> val) {
            children = val;
            return this;
        }

        public Builder registeredBy(Approver val) {
            registeredBy = val;
            return this;
        }

        public Asset build() {
            return new Asset(this);
        }
    }
}
