package com.globaltrade.scm.timer;

import com.globaltrade.scm.entity.Shipment;
import com.globaltrade.scm.entity.ShipmentStatus;
import com.globaltrade.scm.entity.Vendor;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.UserTransaction;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Logger;

@RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class VendorPerformanceRecalculationBean {

    private static final Logger LOG = Logger.getLogger(VendorPerformanceRecalculationBean.class.getName());

    @PersistenceContext(unitName = "supplyChainPU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    public String recalculateSingleVendorScore(Long vendorId) {
        try {
            utx.begin();

            Vendor vendor = em.find(Vendor.class, vendorId);
            if (vendor == null) {
                utx.rollback();
                return "Vendor not found: " + vendorId + " — transaction rolled back.";
            }

            TypedQuery<Shipment> shipQuery = em.createQuery(
                    "SELECT s FROM Shipment s WHERE s.vendor.vendorId = :vendorId "
                            + "AND s.status IN (:delivered, :delayed, :cancelled)",
                    Shipment.class);
            shipQuery.setParameter("vendorId", vendorId);
            shipQuery.setParameter("delivered", ShipmentStatus.DELIVERED);
            shipQuery.setParameter("delayed", ShipmentStatus.DELAYED);
            shipQuery.setParameter("cancelled", ShipmentStatus.CANCELLED);

            List<Shipment> history = shipQuery.getResultList();
            if (history.isEmpty()) {
                utx.commit();
                return "Vendor " + vendor.getVendorCode() + " has no shipment history — nothing to recalculate. Transaction committed (no-op).";
            }

            long onTime = history.stream()
                    .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                    .count();

            BigDecimal score = BigDecimal.valueOf(onTime)
                    .divide(BigDecimal.valueOf(history.size()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            vendor.setPerformanceScore(score);
            em.merge(vendor);

            utx.commit();

            String result = "Vendor " + vendor.getVendorCode() + " score recalculated to " + score
                    + " via BMT (manual commit). " + history.size() + " shipment(s) evaluated.";
            LOG.info(result);
            return result;

        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception rollbackEx) {
                LOG.severe("Rollback also failed: " + rollbackEx.getMessage());
            }
            LOG.severe("BMT recalculation failed, rolled back: " + e.getMessage());
            return "Error during recalculation, transaction rolled back: " + e.getMessage();
        }
    }
}
