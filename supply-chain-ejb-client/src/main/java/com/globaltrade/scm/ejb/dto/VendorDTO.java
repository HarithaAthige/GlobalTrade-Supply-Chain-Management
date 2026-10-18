package com.globaltrade.scm.ejb.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class VendorDTO implements Serializable {

    private Long vendorId;
    private String vendorName;
    private String vendorCode;
    private String country;
    private String contactEmail;
    private String contactPhone;
    private BigDecimal performanceScore;
    private String status;

    public VendorDTO() {
    }

    public VendorDTO(Long vendorId, String vendorName, String vendorCode, String country,
                     String contactEmail, String contactPhone,
                     BigDecimal performanceScore, String status) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorCode = vendorCode;
        this.country = country;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.performanceScore = performanceScore;
        this.status = status;
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

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public BigDecimal getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(BigDecimal performanceScore) {
        this.performanceScore = performanceScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
