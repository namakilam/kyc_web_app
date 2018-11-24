package com.datamonk.blockchain.webapi.pojo;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class ProjectAsset implements Serializable {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonProperty("id")
    private String id;
    @JsonProperty("activity")
    private String activity;
    @JsonProperty("weightage")
    private Float weightage;
    @JsonProperty("sub_weightage")
    private Float subWeightage;
    @JsonProperty("per_milestone")
    private Float percentageMilestone;
    @JsonProperty("milestone")
    private String milestone;
    @JsonProperty("milestone_value")
    private Integer milestoneValue;
    @JsonProperty("status")
    private Boolean status;
    @JsonProperty("children")
    private List<String> children;
    @JsonProperty("parent")
    private String parent;
    @JsonProperty("status_update_request")
    private StatusUpdateRequest statusUpdateRequest;
    @JsonProperty("owner")
    private String owner;
    @JsonProperty("previous_owner")
    private String previousOwner;


    public ProjectAsset() {

    }

    private ProjectAsset(Builder builder) {
        setId(builder.id);
        setActivity(builder.activity);
        setWeightage(builder.weightage);
        setSubWeightage(builder.subWeightage);
        setPercentageMilestone(builder.percentageMilestone);
        setMilestone(builder.milestone);
        setMilestoneValue(builder.milestoneValue);
        setStatus(builder.status);
        setChildren(builder.children);
        setParent(builder.parent);
        setStatusUpdateRequest(builder.statusUpdateRequest);
        setOwner(builder.owner);
        setPreviousOwner(builder.previousOwner);
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

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Float getWeightage() {
        return weightage;
    }

    public void setWeightage(Float weightage) {
        this.weightage = weightage;
    }

    public Float getSubWeightage() {
        return subWeightage;
    }

    public void setSubWeightage(Float subWeightage) {
        this.subWeightage = subWeightage;
    }

    public Float getPercentageMilestone() {
        return percentageMilestone;
    }

    public void setPercentageMilestone(Float percentageMilestone) {
        this.percentageMilestone = percentageMilestone;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public Integer getMilestoneValue() {
        return milestoneValue;
    }

    public void setMilestoneValue(Integer milestoneValue) {
        this.milestoneValue = milestoneValue;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public StatusUpdateRequest getStatusUpdateRequest() {
        return statusUpdateRequest;
    }

    public void setStatusUpdateRequest(StatusUpdateRequest statusUpdateRequest) {
        this.statusUpdateRequest = statusUpdateRequest;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPreviousOwner() {
        return previousOwner;
    }

    public void setPreviousOwner(String previousOwner) {
        this.previousOwner = previousOwner;
    }

    public static ProjectAsset toAsset(String value) throws IOException {
        return objectMapper.readValue(value, ProjectAsset.class);
    }

    public static String toJsonString(ProjectAsset asset) throws IOException {
        return objectMapper.writeValueAsString(asset);
    }

    @Override
    public String toString() {
        return "ProjectAsset{" +
                "id='" + id + '\'' +
                ", activity='" + activity + '\'' +
                ", weightage=" + weightage +
                ", subWeightage=" + subWeightage +
                ", percentageMilestone=" + percentageMilestone +
                ", milestone='" + milestone + '\'' +
                ", milestoneValue=" + milestoneValue +
                ", status=" + status +
                ", children=" + children +
                ", parent='" + parent + '\'' +
                ", statusUpdateRequest=" + statusUpdateRequest +
                '}';
    }

    public static final class Builder {
        private String id;
        private String activity;
        private Float weightage;
        private Float subWeightage;
        private Float percentageMilestone;
        private String milestone;
        private Integer milestoneValue;
        private Boolean status;
        private List<String> children;
        private String parent;
        private StatusUpdateRequest statusUpdateRequest;
        private String owner;
        private String previousOwner;

        private Builder() {
        }

        public Builder id(String val) {
            id = val;
            return this;
        }

        public Builder activity(String val) {
            activity = val;
            return this;
        }

        public Builder weightage(Float val) {
            weightage = val;
            return this;
        }

        public Builder subWeightage(Float val) {
            subWeightage = val;
            return this;
        }

        public Builder percentageMilestone(Float val) {
            percentageMilestone = val;
            return this;
        }

        public Builder milestone(String val) {
            milestone = val;
            return this;
        }

        public Builder milestoneValue(Integer val) {
            milestoneValue = val;
            return this;
        }

        public Builder status(Boolean val) {
            status = val;
            return this;
        }

        public Builder children(List<String> val) {
            children = val;
            return this;
        }

        public Builder parent(String val) {
            parent = val;
            return this;
        }

        public Builder statusUpdateRequest(StatusUpdateRequest val) {
            statusUpdateRequest = val;
            return this;
        }

        public Builder owner(String val) {
            owner = val;
            return this;
        }

        public Builder previousOwner(String val) {
            previousOwner = val;
            return this;
        }

        public ProjectAsset build() {
            return new ProjectAsset(this);
        }
    }
}
