package com.datamonk.blockchain.hyperledger.exceptions;

import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.Peer;

import static com.datamonk.blockchain.config.Util.out;

/**
 * Created by namakilam on 05/08/17.
 */
public class FailedQueryProposalException extends Exception {
    private Boolean verified;
    private ChaincodeResponse.Status status;
    private String message;
    private Peer peer;

    public FailedQueryProposalException(Peer peer, Boolean isVerified, ChaincodeResponse.Status status, String message) {
        this.peer = peer;
        this.verified = isVerified;
        this.status =  status;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return String.format("Failed query proposal from peer %s. status: %d. Messages: %s. Was verified : %s", peer.getName(), status.getStatus(), message, verified.toString());
    }
}
