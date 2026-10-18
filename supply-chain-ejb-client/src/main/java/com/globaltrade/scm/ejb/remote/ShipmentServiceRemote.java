package com.globaltrade.scm.ejb.remote;

import com.globaltrade.scm.ejb.dto.ShipmentDTO;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.exception.ShipmentNotFoundException;
import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;
import jakarta.ejb.Remote;
import java.time.LocalDate;
import java.util.List;

@Remote
public interface ShipmentServiceRemote {

    Long createShipment(String trackingNumber, Long vendorId, String originCountry,
                        String destinationCountry, String carrierName,
                        LocalDate expectedDeliveryDate, LocalDate customsDeadline)
            throws VendorNotFoundException, InvalidVendorStatusException;

    ShipmentDTO getShipment(Long shipmentId) throws ShipmentNotFoundException;

    List<ShipmentDTO> getAllShipments();

    List<ShipmentDTO> getShipmentsByStatus(String status);

    List<ShipmentDTO> getShipmentsPastCustomsDeadline();

    void updateShipmentStatus(Long shipmentId, String status) throws ShipmentNotFoundException;

    void markDelivered(Long shipmentId, LocalDate actualDeliveryDate) throws ShipmentNotFoundException;

    void deleteShipment(Long shipmentId) throws ShipmentNotFoundException;
}
