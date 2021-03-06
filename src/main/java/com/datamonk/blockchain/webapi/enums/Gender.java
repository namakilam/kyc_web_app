package com.datamonk.blockchain.webapi.enums;

import com.datamonk.blockchain.hyperledger.exceptions.InvalidGenderException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by namakilam on 05/08/17.
 */
public enum Gender implements Serializable{
    MALE, FEMALE;

    private static Map<String, Gender> stringGenderMap = new HashMap<String, Gender>() {
        {
            put("male", MALE);
            put("female", FEMALE);
        }
    };

    @JsonCreator
    public static Gender forValue(String value) throws InvalidGenderException {
        if (stringGenderMap.containsKey(value.toLowerCase())) {
            return stringGenderMap.get(value.toLowerCase());
        } else {
            throw new InvalidGenderException();
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
