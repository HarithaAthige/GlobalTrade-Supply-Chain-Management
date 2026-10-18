package com.globaltrade.scm.timer;

import com.globaltrade.scm.entity.Shipment;
import com.globaltrade.scm.entity.ShipmentStatus;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;


import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.ZoneId;

@Singleton
@Startup
public class TimerCoordinatorBean {

    private static final Logger LOG = Logger.getLogger(TimerCoordinatorBean.class.getName());

    @Resource
    private SessionContext sessionContext;

    @PersistenceContext(unitName = "supplyChainPU")
    private EntityManager em;

    private static final Duration ROUTE_SWEEP_INTERVAL = Duration.ofMinutes(30);

    @PostConstruct
    public void init() {
        ensureRouteOptimizationTimerExists();
        recoverCustomsDeadlineTimersOnBoot();
    }


    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    public void scheduleCustomsDeadlineTimer(Long shipmentId, LocalDate customsDeadline) {
        if (customsDeadline == null) {
            return; // nothing to schedule, e.g. domestic shipments with no customs step
        }
        if (customsDeadline.isBefore(LocalDate.now())) {
            LOG.warning("Customs deadline for shipment " + shipmentId
                    + " is already in the past; skipping timer creation.");
            return;
        }

        Date deadlineAsDate = Date.from(
                customsDeadline.atStartOfDay(ZoneId.systemDefault()).toInstant());

        TimerConfig config = new TimerConfig(
                new TimerPayload(TimerPayload.TimerType.CUSTOMS_DEADLINE, shipmentId),
                true
        );
        sessionContext.getTimerService().createSingleActionTimer(deadlineAsDate, config);
        LOG.info("Scheduled customs deadline timer for shipment " + shipmentId
                + " at " + deadlineAsDate);
    }

    @RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR"})
    public void cancelCustomsDeadlineTimer(Long shipmentId) {
        for (Timer timer : sessionContext.getTimerService().getTimers()) {
            Object info = timer.getInfo();
            if (info instanceof TimerPayload payload
                    && payload.getType() == TimerPayload.TimerType.CUSTOMS_DEADLINE
                    && payload.getEntityId().equals(shipmentId)) {
                timer.cancel();
                LOG.info("Cancelled customs deadline timer for shipment " + shipmentId);
            }
        }
    }


    @PermitAll
    @Timeout
    public void handleTimeout(Timer timer) {
        Object info = timer.getInfo();
        if (!(info instanceof TimerPayload payload)) {
            LOG.warning("Timer fired with unrecognised info: " + info);
            return;
        }

        try {
            switch (payload.getType()) {
                case CUSTOMS_DEADLINE -> handleCustomsDeadline(payload.getEntityId());
                case ROUTE_OPTIMIZATION_SWEEP -> handleRouteOptimizationSweep();
            }
        } catch (Exception e) {

            LOG.log(Level.SEVERE, "Timer handler failed for " + payload, e);
        }
    }


    private void handleCustomsDeadline(Long shipmentId) {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            LOG.warning("Customs deadline fired for shipment " + shipmentId + " but it no longer exists.");
            return;
        }
        if (shipment.getStatus() == ShipmentStatus.DELIVERED
                || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            return;
        }
        if (shipment.getStatus() != ShipmentStatus.CUSTOMS_HOLD) {
            LOG.info("Shipment " + shipmentId + " missed its customs deadline — flagging CUSTOMS_HOLD.");
            shipment.setStatus(ShipmentStatus.CUSTOMS_HOLD);
            em.merge(shipment);
            // picked up by the audit-logging interceptor automatically.
        }
    }

    private void handleRouteOptimizationSweep() {
        TypedQuery<Shipment> query = em.createQuery(
                "SELECT s FROM Shipment s WHERE s.status = :inTransit",
                Shipment.class);
        query.setParameter("inTransit", ShipmentStatus.IN_TRANSIT);
        List<Shipment> active = query.getResultList();

        for (Shipment s : active) {

            if (s.getExpectedDeliveryDate() != null
                    && s.getExpectedDeliveryDate().isBefore(LocalDate.now())
                    && s.getStatus() != ShipmentStatus.DELAYED) {
                s.setStatus(ShipmentStatus.DELAYED);
                em.merge(s);
                LOG.info("Route sweep: shipment " + s.getShipmentId() + " marked DELAYED (past expected delivery).");
            }
        }
        LOG.info("Route optimization sweep complete — " + active.size() + " active shipments evaluated.");
    }


    private void ensureRouteOptimizationTimerExists() {
        boolean exists = sessionContext.getTimerService().getTimers().stream()
                .anyMatch(t -> t.getInfo() instanceof TimerPayload p
                        && p.getType() == TimerPayload.TimerType.ROUTE_OPTIMIZATION_SWEEP);
        if (exists) {
            LOG.info("Route optimization interval timer already exists — skipping creation.");
            return; // restored from persistent timer store on WildFly restart
        }
        TimerConfig config = new TimerConfig(
                new TimerPayload(TimerPayload.TimerType.ROUTE_OPTIMIZATION_SWEEP, null),
                true
        );
        sessionContext.getTimerService().createIntervalTimer(
                ROUTE_SWEEP_INTERVAL.toMillis(), // initial delay
                ROUTE_SWEEP_INTERVAL.toMillis(), // interval
                config
        );
        LOG.info("Route optimization interval timer created (" + ROUTE_SWEEP_INTERVAL.toMinutes() + " min).");
    }


    private void recoverCustomsDeadlineTimersOnBoot() {
        Collection<Timer> existing = sessionContext.getTimerService().getTimers();
        List<Shipment> pending = em.createQuery(
                        "SELECT s FROM Shipment s WHERE s.customsDeadline IS NOT NULL "
                                + "AND s.status NOT IN (:delivered, :cancelled)",
                        Shipment.class)
                .setParameter("delivered", ShipmentStatus.DELIVERED)
                .setParameter("cancelled", ShipmentStatus.CANCELLED)
                .getResultList();

        for (Shipment s : pending) {
            boolean alreadyScheduled = existing.stream().anyMatch(t ->
                    t.getInfo() instanceof TimerPayload p
                            && p.getType() == TimerPayload.TimerType.CUSTOMS_DEADLINE
                            && p.getEntityId().equals(s.getShipmentId()));
            if (!alreadyScheduled) {
                scheduleCustomsDeadlineTimer(s.getShipmentId(), s.getCustomsDeadline());
            }
        }
    }
}