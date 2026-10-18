package com.globaltrade.scm.service;

import com.globaltrade.scm.ejb.dto.VendorDTO;
import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.remote.VendorServiceRemote;
import com.globaltrade.scm.entity.Vendor;
import com.globaltrade.scm.entity.VendorStatus;
import jakarta.ejb.EJB;
import jakarta.interceptor.Interceptors;
import com.globaltrade.scm.interceptor.AuditLoggingInterceptor;
import com.globaltrade.scm.interceptor.PerformanceMonitoringInterceptor;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.ejb.SessionContext;
import jakarta.annotation.Resource;


@Stateless
@Interceptors({AuditLoggingInterceptor.class, PerformanceMonitoringInterceptor.class})
public class VendorServiceBean implements VendorServiceRemote {

    @PersistenceContext(unitName = "supplyChainPU")
    private EntityManager em;

    @EJB
    private com.globaltrade.scm.timer.VendorPerformanceRecalculationBean recalculationBean;
    @Resource
    private SessionContext sessionContext;

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    @Override
    public Long createVendor(String vendorName, String vendorCode, String country,
                             String contactEmail, String contactPhone) {
        Vendor vendor = new Vendor();
        vendor.setVendorName(vendorName);
        vendor.setVendorCode(vendorCode);
        vendor.setCountry(country);
        vendor.setContactEmail(contactEmail);
        vendor.setContactPhone(contactPhone);
        vendor.setStatus(VendorStatus.ACTIVE);
        vendor.setPerformanceScore(java.math.BigDecimal.ZERO);

        em.persist(vendor);
        em.flush();

        return vendor.getVendorId();
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public VendorDTO getVendor(Long vendorId) throws VendorNotFoundException {
        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor == null) {
            throw new VendorNotFoundException(vendorId);
        }
        return toDTO(vendor);
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<VendorDTO> getAllVendors() {
        List<Vendor> vendors = em.createQuery("SELECT v FROM Vendor v", Vendor.class)
                .getResultList();
        return vendors.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    @Override
    public void updateVendorStatus(Long vendorId, String status)
            throws VendorNotFoundException, InvalidVendorStatusException {
        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor == null) {
            throw new VendorNotFoundException(vendorId);
        }

        VendorStatus newStatus;
        try {
            newStatus = VendorStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidVendorStatusException(vendorId, status, "updateVendorStatus (invalid status value)");
        }

        vendor.setStatus(newStatus);
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    @Override
    public void deleteVendor(Long vendorId) throws VendorNotFoundException {
        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor == null) {
            throw new VendorNotFoundException(vendorId);
        }
        em.remove(vendor);
    }

    @PermitAll
    public String deleteVendorSecureCheck(Long vendorId) {
        if (!sessionContext.isCallerInRole("SYSTEM_ADMIN")) {
            return "Access denied: only SYSTEM_ADMIN can delete vendors. "
                    + "Caller: " + sessionContext.getCallerPrincipal().getName();
        }

        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor == null) {
            return "Vendor not found: " + vendorId;
        }

        em.remove(vendor);
        return "Vendor " + vendor.getVendorCode() + " deleted by " + sessionContext.getCallerPrincipal().getName();
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    @Override
    public String recalculateVendorScoreBMT(Long vendorId) {
        return recalculationBean.recalculateSingleVendorScore(vendorId);
    }

    private VendorDTO toDTO(Vendor vendor) {
        return new VendorDTO(
                vendor.getVendorId(),
                vendor.getVendorName(),
                vendor.getVendorCode(),
                vendor.getCountry(),
                vendor.getContactEmail(),
                vendor.getContactPhone(),
                vendor.getPerformanceScore(),
                vendor.getStatus().name()
        );
    }

}
