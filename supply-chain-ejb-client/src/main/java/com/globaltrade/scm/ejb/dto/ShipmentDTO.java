package com.globaltrade.scm.ejb.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class ShipmentDTO implements Serializable {

    private Long shipmentId;
    private String trackingNumber;
    private Long vendorId;
    private String vendorName;
    private String originCountry;
    private String destinationCountry;
    private String carrierName;
    private String status;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private LocalDate customsDeadline;

    public ShipmentDTO() {
    }

    public ShipmentDTO(Long shipmentId, String trackingNumber, Long vendorId, String vendorName,
                       String originCountry, String destinationCountry, String carrierName,
                       String status, LocalDate expectedDeliveryDate,
                       LocalDate actualDeliveryDate, LocalDate customsDeadline) {
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.originCountry = originCountry;
        this.destinationCountry = destinationCountry;
        this.carrierName = carrierName;
        this.status = status;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.actualDeliveryDate = actualDeliveryDate;
        this.customsDeadline = customsDeadline;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public LocalDate getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(LocalDate actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public LocalDate getCustomsDeadline() {
        return customsDeadline;
    }

    public void setCustomsDeadline(LocalDate customsDeadline) {
        this.customsDeadline = customsDeadline;
    }
}
