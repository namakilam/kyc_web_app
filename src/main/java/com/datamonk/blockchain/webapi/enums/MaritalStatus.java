package com.datamonk.blockchain.webapi.enums;

import com.datamonk.blockchain.hyperledger.exceptions.InvalidMaritalStatusException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by namakilam on 05/08/17.
 */
public enum MaritalStatus {
    MARRIED,
    UNMARRIED;

    private static Map<String, MaritalStatus> stringMaritalStatusMap = new HashMap<String, MaritalStatus>() {
        {
            put("married", MARRIED);
            put("unmarried", UNMARRIED);
        }
    };

    @JsonCreator
    public static MaritalStatus forValue(String value) throws InvalidMaritalStatusException {
        if (stringMaritalStatusMap.containsKey(value.toLowerCase())) {
            return stringMaritalStatusMap.get(value.toLowerCase());
        } else {
            throw new InvalidMaritalStatusException();
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
