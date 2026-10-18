package com.globaltrade.scm.ejb.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class ShipmentNotFoundException extends SupplyChainException {

    private static final long serialVersionUID = 1L;

    public ShipmentNotFoundException(Long shipmentId) {
        super("Shipment not found: " + shipmentId);
    }
}
