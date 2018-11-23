package com.datamonk.blockchain.webapi.pojo;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class StatusUpdateRequest implements Serializable {
    @JsonProperty("new_status")
    private Boolean newStatus;

    @JsonProperty("approval_status")
    private String approvalStatus;

    @JsonProperty("responder")
    private String responder;

    public StatusUpdateRequest() {

    }

    private StatusUpdateRequest(Builder builder) {
        setNewStatus(builder.newStatus);
        setApprovalStatus(builder.approvalStatus);
        setResponder(builder.responder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Boolean getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Boolean newStatus) {
        this.newStatus = newStatus;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getResponder() {
        return responder;
    }

    public void setResponder(String responder) {
        this.responder = responder;
    }


    public static final class Builder {
        private Boolean newStatus;
        private String approvalStatus;
        private String responder;

        private Builder() {
        }

        public Builder newStatus(Boolean val) {
            newStatus = val;
            return this;
        }

        public Builder approvalStatus(String val) {
            approvalStatus = val;
            return this;
        }

        public Builder responder(String val) {
            responder = val;
            return this;
        }

        public StatusUpdateRequest build() {
            return new StatusUpdateRequest(this);
        }
    }

    @Override
    public String toString() {
        return "StatusUpdateRequest{" +
                "newStatus=" + newStatus +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", responder='" + responder + '\'' +
                '}';
    }
}
