package com.datamonk.blockchain.webapi.enums;

import com.datamonk.blockchain.hyperledger.exceptions.InvalidGenderException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by namakilam on 11/08/17.
 */
public enum AssetType {
    PROPERTY, LAND;

    private static Map<String, AssetType> stringAssetTypeMap = new HashMap<String, AssetType>() {
        {
            put("property", PROPERTY);
            put("land", LAND);
        }
    };

    @JsonCreator
    public static AssetType forValue(String value) throws InvalidGenderException {
        if (stringAssetTypeMap.containsKey(value.toLowerCase())) {
            return stringAssetTypeMap.get(value.toLowerCase());
        } else {
            throw new InvalidGenderException();
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
