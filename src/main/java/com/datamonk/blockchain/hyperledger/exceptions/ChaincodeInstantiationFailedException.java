package com.datamonk.blockchain.hyperledger.exceptions;

/**
 * Created by namakilam on 04/08/17.
 */
public class ChaincodeInstantiationFailedException extends Exception {
    public ChaincodeInstantiationFailedException() {

    }

    public ChaincodeInstantiationFailedException(Throwable throwable) {
        super(throwable);
    }
}
