package com.globaltrade.scm.service;

import com.globaltrade.scm.ejb.dto.InventoryItemDTO;
import com.globaltrade.scm.ejb.remote.InventoryServiceRemote;
import com.globaltrade.scm.entity.InventoryItem;
import com.globaltrade.scm.entity.Vendor;
import jakarta.interceptor.Interceptors;
import com.globaltrade.scm.interceptor.AuditLoggingInterceptor;
import com.globaltrade.scm.interceptor.ValidateVendorActive;
import com.globaltrade.scm.interceptor.VendorValidationInterceptor;
import com.globaltrade.scm.interceptor.PerformanceMonitoringInterceptor;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.exception.InventoryItemNotFoundException;
import com.globaltrade.scm.ejb.exception.InsufficientInventoryException;
import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.security.RolesAllowed;


@Stateless
@Interceptors({AuditLoggingInterceptor.class, VendorValidationInterceptor.class, PerformanceMonitoringInterceptor.class})
public class InventoryServiceBean implements InventoryServiceRemote {

    @PersistenceContext(unitName = "supplyChainPU")
    private EntityManager em;

    @RolesAllowed({"SYSTEM_ADMIN", "WAREHOUSE_MANAGER"})
    @Override
    public Long createItem(String sku, String itemName, @ValidateVendorActive Long vendorId,
                           Integer quantityOnHand, Integer reorderThreshold,
                           String warehouseLocation, BigDecimal unitPrice)
            throws VendorNotFoundException, InvalidVendorStatusException {

        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor == null) {
            throw new VendorNotFoundException(vendorId);
        }

        InventoryItem item = new InventoryItem();
        item.setSku(sku);
        item.setItemName(itemName);
        item.setVendor(vendor);
        item.setQuantityOnHand(quantityOnHand);
        item.setReorderThreshold(reorderThreshold);
        item.setWarehouseLocation(warehouseLocation);
        item.setUnitPrice(unitPrice);
        item.setLastRestockedAt(LocalDateTime.now());

        em.persist(item);
        em.flush();

        return item.getItemId();
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public InventoryItemDTO getItem(Long itemId) throws InventoryItemNotFoundException {
        InventoryItem item = em.find(InventoryItem.class, itemId);
        if (item == null) {
            throw new InventoryItemNotFoundException(itemId);
        }
        return toDTO(item);
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<InventoryItemDTO> getAllItems() {
        List<InventoryItem> items = em.createQuery(
                        "SELECT i FROM InventoryItem i", InventoryItem.class)
                .getResultList();
        return items.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<InventoryItemDTO> getItemsBelowReorderThreshold() {
        List<InventoryItem> items = em.createQuery(
                        "SELECT i FROM InventoryItem i WHERE i.quantityOnHand <= i.reorderThreshold",
                        InventoryItem.class)
                .getResultList();
        return items.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @RolesAllowed({"SYSTEM_ADMIN", "WAREHOUSE_MANAGER"})
    @Override
    public void adjustQuantity(Long itemId, Integer delta)
            throws InventoryItemNotFoundException, InsufficientInventoryException {
        InventoryItem item = em.find(InventoryItem.class, itemId);
        if (item == null) {
            throw new InventoryItemNotFoundException(itemId);
        }

        int newQty = item.getQuantityOnHand() + delta;
        if (newQty < 0) {
            throw new InsufficientInventoryException(itemId, Math.abs(delta), item.getQuantityOnHand());
        }

        item.setQuantityOnHand(newQty);
        if (delta > 0) {
            item.setLastRestockedAt(LocalDateTime.now());
        }
    }

    @RolesAllowed({"SYSTEM_ADMIN", "WAREHOUSE_MANAGER"})
    @Override
    public void deleteItem(Long itemId) throws InventoryItemNotFoundException {
        InventoryItem item = em.find(InventoryItem.class, itemId);
        if (item == null) {
            throw new InventoryItemNotFoundException(itemId);
        }
        em.remove(item);
    }

    private InventoryItemDTO toDTO(InventoryItem item) {
        return new InventoryItemDTO(
                item.getItemId(),
                item.getSku(),
                item.getItemName(),
                item.getVendor().getVendorId(),
                item.getVendor().getVendorName(),
                item.getQuantityOnHand(),
                item.getReorderThreshold(),
                item.getWarehouseLocation(),
                item.getUnitPrice(),
                item.isBelowReorderThreshold()
        );
    }
}
