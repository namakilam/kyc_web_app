package com.datamonk.blockchain.hyperledger.exceptions;

/**
 * Created by namakilam on 04/08/17.
 */
public class NotEnoughEndorsersException extends Exception {
    private String message;

    public NotEnoughEndorsersException(String message) {
        super(message);
        this.message = message;
    }

    public NotEnoughEndorsersException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
