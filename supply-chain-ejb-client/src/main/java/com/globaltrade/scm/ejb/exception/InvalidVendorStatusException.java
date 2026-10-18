package com.globaltrade.scm.ejb.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class InvalidVendorStatusException extends SupplyChainException {

    private static final long serialVersionUID = 1L;

    public InvalidVendorStatusException(Long vendorId, String currentStatus, String attemptedAction) {
        super("Vendor " + vendorId + " has status " + currentStatus
                + " — cannot perform action: " + attemptedAction);
    }
}
