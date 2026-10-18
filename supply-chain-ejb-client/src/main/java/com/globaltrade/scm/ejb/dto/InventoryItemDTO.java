package com.globaltrade.scm.ejb.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class InventoryItemDTO implements Serializable {

    private Long itemId;
    private String sku;
    private String itemName;
    private Long vendorId;
    private String vendorName;
    private Integer quantityOnHand;
    private Integer reorderThreshold;
    private String warehouseLocation;
    private BigDecimal unitPrice;
    private boolean belowReorderThreshold;

    public InventoryItemDTO() {
    }

    public InventoryItemDTO(Long itemId, String sku, String itemName, Long vendorId, String vendorName,
                            Integer quantityOnHand, Integer reorderThreshold,
                            String warehouseLocation, BigDecimal unitPrice,
                            boolean belowReorderThreshold) {
        this.itemId = itemId;
        this.sku = sku;
        this.itemName = itemName;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.quantityOnHand = quantityOnHand;
        this.reorderThreshold = reorderThreshold;
        this.warehouseLocation = warehouseLocation;
        this.unitPrice = unitPrice;
        this.belowReorderThreshold = belowReorderThreshold;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean isBelowReorderThreshold() {
        return belowReorderThreshold;
    }

    public void setBelowReorderThreshold(boolean belowReorderThreshold) {
        this.belowReorderThreshold = belowReorderThreshold;
    }
}
