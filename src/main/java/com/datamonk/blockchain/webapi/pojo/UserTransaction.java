package com.datamonk.blockchain.webapi.pojo;

import com.datamonk.blockchain.webapi.enums.TransactionStatus;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by namakilam on 23/07/18.
 */
public class UserTransaction {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonProperty("fromId")
    private String fromId;

    @JsonProperty("toId")
    private String toId;

    @JsonProperty("assetId")
    private String propertyId;

    @JsonProperty("status")
    private TransactionStatus transactionStatus;

    public UserTransaction() {
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    @Override
    public String toString() {
        return "UserTransaction{" +
                "fromId='" + fromId + '\'' +
                ", toId='" + toId + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", transactionStatus=" + transactionStatus +
                '}';
    }

    public static String toJsonString(UserTransaction transaction) throws IOException {
        return objectMapper.writeValueAsString(transaction);
    }
}
