package com.datamonk.blockchain.hyperledger.exceptions;

/**
 * Created by namakilam on 05/08/17.
 */
public class InvalidNumberArgumentException extends Exception {
    private Integer required;
    private Integer present;

    public InvalidNumberArgumentException(Integer required, Integer present) {
        this.present = present;
        this.required = required;
    }

    @Override
    public String getMessage() {
        return String.format("Invalid Number Of Arguments. Required : %s, But Present : %s", required, present);
    }
}
