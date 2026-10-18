package com.globaltrade.scm.service;

import com.globaltrade.scm.ejb.dto.ShipmentDTO;
import com.globaltrade.scm.ejb.remote.ShipmentServiceRemote;
import com.globaltrade.scm.entity.Shipment;
import com.globaltrade.scm.entity.ShipmentStatus;
import com.globaltrade.scm.entity.Vendor;
import com.globaltrade.scm.interceptor.VendorValidationInterceptor;
import jakarta.interceptor.Interceptors;
import com.globaltrade.scm.interceptor.AuditLoggingInterceptor;
import com.globaltrade.scm.interceptor.PerformanceMonitoringInterceptor;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.exception.ShipmentNotFoundException;
import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.annotation.security.RolesAllowed;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.globaltrade.scm.timer.TimerCoordinatorBean;
import jakarta.ejb.EJB;


@Stateless
@Interceptors({AuditLoggingInterceptor.class, VendorValidationInterceptor.class, PerformanceMonitoringInterceptor.class})
public class ShipmentServiceBean implements ShipmentServiceRemote {

    @PersistenceContext(unitName = "supplyChainPU")
    private EntityManager em;

    @EJB
    private TimerCoordinatorBean timerCoordinator;

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    @Override
    public Long createShipment(String trackingNumber, @com.globaltrade.scm.interceptor.ValidateVendorActive Long vendorId, String originCountry,
                               String destinationCountry, String carrierName,
                               LocalDate expectedDeliveryDate, LocalDate customsDeadline)
            throws VendorNotFoundException, InvalidVendorStatusException {

        Vendor vendor = em.find(Vendor.class, vendorId);
        if (vendor == null) {
            throw new VendorNotFoundException(vendorId);
        }

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setVendor(vendor);
        shipment.setOriginCountry(originCountry);
        shipment.setDestinationCountry(destinationCountry);
        shipment.setCarrierName(carrierName);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setExpectedDeliveryDate(expectedDeliveryDate);
        shipment.setCustomsDeadline(customsDeadline);

        em.persist(shipment);
        em.flush();

        timerCoordinator.scheduleCustomsDeadlineTimer(shipment.getShipmentId(), shipment.getCustomsDeadline());

        return shipment.getShipmentId();
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public ShipmentDTO getShipment(Long shipmentId) throws ShipmentNotFoundException {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new ShipmentNotFoundException(shipmentId);
        }
        return toDTO(shipment);
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<ShipmentDTO> getAllShipments() {
        List<Shipment> shipments = em.createQuery(
                        "SELECT s FROM Shipment s", Shipment.class)
                .getResultList();
        return shipments.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<ShipmentDTO> getShipmentsByStatus(String status) {
        List<Shipment> shipments = em.createQuery(
                        "SELECT s FROM Shipment s WHERE s.status = :status", Shipment.class)
                .setParameter("status", ShipmentStatus.valueOf(status))
                .getResultList();
        return shipments.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public List<ShipmentDTO> getShipmentsPastCustomsDeadline() {
        List<Shipment> shipments = em.createQuery(
                        "SELECT s FROM Shipment s WHERE s.customsDeadline < :today " +
                                "AND s.status NOT IN (:delivered, :cancelled)", Shipment.class)
                .setParameter("today", LocalDate.now())
                .setParameter("delivered", ShipmentStatus.DELIVERED)
                .setParameter("cancelled", ShipmentStatus.CANCELLED)
                .getResultList();
        return shipments.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "CUSTOMS_AGENT"})
    @Override
    public void updateShipmentStatus(Long shipmentId, String status) throws ShipmentNotFoundException {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new ShipmentNotFoundException(shipmentId);
        }

        shipment.setStatus(ShipmentStatus.valueOf(status));
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    @Override
    public void markDelivered(Long shipmentId, LocalDate actualDeliveryDate) throws ShipmentNotFoundException {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new ShipmentNotFoundException(shipmentId);
        }
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setActualDeliveryDate(actualDeliveryDate);
        timerCoordinator.cancelCustomsDeadlineTimer(shipmentId);
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    @Override
    public void deleteShipment(Long shipmentId) throws ShipmentNotFoundException {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new ShipmentNotFoundException(shipmentId);
        }
        em.remove(shipment);
        timerCoordinator.cancelCustomsDeadlineTimer(shipmentId);
    }

    private ShipmentDTO toDTO(Shipment shipment) {
        return new ShipmentDTO(
                shipment.getShipmentId(),
                shipment.getTrackingNumber(),
                shipment.getVendor().getVendorId(),
                shipment.getVendor().getVendorName(),
                shipment.getOriginCountry(),
                shipment.getDestinationCountry(),
                shipment.getCarrierName(),
                shipment.getStatus().name(),
                shipment.getExpectedDeliveryDate(),
                shipment.getActualDeliveryDate(),
                shipment.getCustomsDeadline()
        );
    }
}
