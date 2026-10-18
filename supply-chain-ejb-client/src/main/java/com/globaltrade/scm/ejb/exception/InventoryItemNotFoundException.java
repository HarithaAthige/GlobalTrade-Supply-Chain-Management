package com.globaltrade.scm.ejb.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class InventoryItemNotFoundException extends SupplyChainException {

    private static final long serialVersionUID = 1L;

    public InventoryItemNotFoundException(Long itemId) {
        super("Inventory item not found: " + itemId);
    }
}
