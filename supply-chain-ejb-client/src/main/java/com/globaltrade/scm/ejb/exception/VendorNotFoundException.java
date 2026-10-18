package com.globaltrade.scm.ejb.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class VendorNotFoundException extends SupplyChainException {

    private static final long serialVersionUID = 1L;

    public VendorNotFoundException(Long vendorId) {
        super("Vendor not found: " + vendorId);
    }
}
