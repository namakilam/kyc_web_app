package com.datamonk.blockchain.webapi.pojo;

import com.datamonk.blockchain.webapi.enums.Gender;
import com.datamonk.blockchain.webapi.enums.MaritalStatus;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by namakilam on 05/08/17.
 */
public class KYCUser implements Serializable{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String name;

    private Gender gender;

    @JsonProperty("dob")
    private String DOB;

    @JsonProperty("aadhar_no")
    private String aadharNumber;

    @JsonProperty("pan_no")
    private String panNumber;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("cibil_score")
    private Integer cibilScore;

    @JsonProperty("marital_status")
    private MaritalStatus maritalStatus;

    public KYCUser() {

    }

    private KYCUser(Builder builder) {
        this.name = builder.name;
        this.gender = builder.gender;
        this.DOB = builder.DOB;
        this.aadharNumber = builder.aadharNumber;
        this.panNumber = builder.panNumber;
        this.address = builder.address;
        this.cibilScore = builder.cibilScore;
        this.maritalStatus = builder.maritalStatus;
    }

    public static Builder newKYCUser() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Integer getCibilScore() {
        return cibilScore;
    }

    public void setCibilScore(Integer cibilScore) {
        this.cibilScore = cibilScore;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public static final class Builder {
        private String name;
        private Gender gender;
        private String DOB;
        private String aadharNumber;
        private String panNumber;
        private Address address;
        private Integer cibilScore;
        private MaritalStatus maritalStatus;

        private Builder() {
        }

        public KYCUser build() {
            return new KYCUser(this);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public Builder DOB(String DOB) {
            this.DOB = DOB;
            return this;
        }

        public Builder aadharNumber(String aadharNumber) {
            this.aadharNumber = aadharNumber;
            return this;
        }

        public Builder panNumber(String panNumber) {
            this.panNumber = panNumber;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder cibilScore(Integer cibilScore) {
            this.cibilScore = cibilScore;
            return this;
        }

        public Builder maritalStatus(MaritalStatus maritalStatus) {
            this.maritalStatus = maritalStatus;
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

    public static KYCUser toKYCUser(String arg) throws IOException {
        return objectMapper.readValue(arg, KYCUser.class);
    }

    public static String toJsonString(KYCUser user) throws IOException {
        return objectMapper.writeValueAsString(user);
    }
}
