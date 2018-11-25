package com.datamonk.blockchain.webapi.pojo;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class Milestone implements Serializable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("milestone_value")
    private String milestoneValue;
    @JsonProperty("per_milestone")
    private Float perMilestone;
    @JsonProperty("status")
    private Boolean status;

    public Milestone() {

    }

    private Milestone(Builder builder) {
        setId(builder.id);
        setMilestoneValue(builder.value);
        setPerMilestone(builder.perMilestone);
        setStatus(builder.status);
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

    public String getMilestoneValue() {
        return milestoneValue;
    }

    public void setMilestoneValue(String milestoneValue) {
        this.milestoneValue = milestoneValue;
    }

    public Float getPerMilestone() {
        return perMilestone;
    }

    public void setPerMilestone(Float perMilestone) {
        this.perMilestone = perMilestone;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }


    public static final class Builder {
        private String id;
        private String value;
        private Float perMilestone;
        private Boolean status;

        private Builder() {
        }

        public Builder id(String val) {
            id = val;
            return this;
        }

        public Builder value(String val) {
            value = val;
            return this;
        }

        public Builder perMilestone(Float val) {
            perMilestone = val;
            return this;
        }

        public Builder status(Boolean val) {
            status = val;
            return this;
        }

        public Milestone build() {
            return new Milestone(this);
        }
    }
}
