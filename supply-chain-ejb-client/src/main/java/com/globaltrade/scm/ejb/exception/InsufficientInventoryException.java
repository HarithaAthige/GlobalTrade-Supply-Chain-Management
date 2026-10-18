package com.globaltrade.scm.ejb.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class InsufficientInventoryException extends SupplyChainException {

    private static final long serialVersionUID = 1L;

    public InsufficientInventoryException(Long itemId, int requested, int available) {
        super("Insufficient inventory for item " + itemId + ": requested "
                + requested + ", only " + available + " available");
    }
}
