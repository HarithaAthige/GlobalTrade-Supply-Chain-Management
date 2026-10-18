package com.globaltrade.scm.ejb.remote;

import com.globaltrade.scm.ejb.dto.InventoryItemDTO;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.exception.InventoryItemNotFoundException;
import com.globaltrade.scm.ejb.exception.InsufficientInventoryException;
import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;
import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.util.List;

@Remote
public interface InventoryServiceRemote {

    Long createItem(String sku, String itemName, Long vendorId,
                    Integer quantityOnHand, Integer reorderThreshold,
                    String warehouseLocation, BigDecimal unitPrice)
            throws VendorNotFoundException, InvalidVendorStatusException;

    InventoryItemDTO getItem(Long itemId) throws InventoryItemNotFoundException;

    List<InventoryItemDTO> getAllItems();

    List<InventoryItemDTO> getItemsBelowReorderThreshold();

    void adjustQuantity(Long itemId, Integer delta)
            throws InventoryItemNotFoundException, InsufficientInventoryException;

    void deleteItem(Long itemId) throws InventoryItemNotFoundException;
}
