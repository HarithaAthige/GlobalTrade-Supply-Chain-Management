package com.globaltrade.scm.timer;

import com.globaltrade.scm.entity.InventoryItem;
import com.globaltrade.scm.entity.Shipment;
import com.globaltrade.scm.entity.ShipmentStatus;
import com.globaltrade.scm.entity.Vendor;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Logger;


@Singleton
public class ScheduledMaintenanceBean {

    private static final Logger LOG = Logger.getLogger(ScheduledMaintenanceBean.class.getName());

    @PersistenceContext(unitName = "supplyChainPU")
    private EntityManager em;

    @Schedule(hour = "*", minute = "*/15", second = "0", persistent = true,
            info = "shipment-status-sweep")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sweepShipmentStatuses() {
        TypedQuery<Shipment> query = em.createQuery(
                "SELECT s FROM Shipment s WHERE s.status IN (:pending, :inTransit) "
                        + "AND s.expectedDeliveryDate < CURRENT_TIMESTAMP",
                Shipment.class);
        query.setParameter("pending", ShipmentStatus.PENDING);
        query.setParameter("inTransit", ShipmentStatus.IN_TRANSIT);

        List<Shipment> overdue = query.getResultList();
        for (Shipment s : overdue) {
            s.setStatus(ShipmentStatus.DELAYED);
            em.merge(s);
        }
        if (!overdue.isEmpty()) {
            LOG.info("Shipment status sweep: " + overdue.size() + " shipment(s) marked DELAYED.");
        }
    }


    @Schedule(hour = "*", minute = "0", second = "0", persistent = true,
            info = "inventory-reorder-check")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void checkInventoryLevels() {
        TypedQuery<InventoryItem> query = em.createQuery(
                "SELECT i FROM InventoryItem i WHERE i.quantityOnHand <= i.reorderThreshold",
                InventoryItem.class);
        List<InventoryItem> lowStock = query.getResultList();

        for (InventoryItem item : lowStock) {
            LOG.warning("Reorder alert: SKU " + item.getSku() + " (" + item.getItemName()
                    + ") at " + item.getQuantityOnHand() + " units, threshold "
                    + item.getReorderThreshold());
        }
        LOG.info("Inventory check complete — " + lowStock.size() + " item(s) below reorder threshold.");
    }


    @Schedule(hour = "1", minute = "0", second = "0", persistent = true,
            info = "vendor-performance-evaluation")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void evaluateVendorPerformance() {
        List<Vendor> vendors = em.createQuery("SELECT v FROM Vendor v", Vendor.class).getResultList();

        for (Vendor vendor : vendors) {
            TypedQuery<Shipment> shipQuery = em.createQuery(
                    "SELECT s FROM Shipment s WHERE s.vendor.vendorId = :vendorId "
                            + "AND s.status IN (:delivered, :delayed, :cancelled)",
                    Shipment.class);
            shipQuery.setParameter("vendorId", vendor.getVendorId());
            shipQuery.setParameter("delivered", ShipmentStatus.DELIVERED);
            shipQuery.setParameter("delayed", ShipmentStatus.DELAYED);
            shipQuery.setParameter("cancelled", ShipmentStatus.CANCELLED);

            List<Shipment> history = shipQuery.getResultList();
            if (history.isEmpty()) {
                continue; // no shipment history yet — leave existing score untouched
            }

            long onTime = history.stream()
                    .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                    .count();
            long delayedOrCancelled = history.stream()
                    .filter(s -> s.getStatus() == ShipmentStatus.DELAYED || s.getStatus() == ShipmentStatus.CANCELLED)
                    .count();

            BigDecimal score = BigDecimal.valueOf(onTime)
                    .divide(BigDecimal.valueOf(history.size()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            vendor.setPerformanceScore(score);
            em.merge(vendor);

            LOG.info("Vendor " + vendor.getVendorCode() + " performance score recalculated: " + score
                    + " (" + onTime + " delivered, " + delayedOrCancelled + " delayed/cancelled, "
                    + history.size() + " total).");
        }
    }
}

