package com.globaltrade.scm.interceptor;

import com.globaltrade.scm.entity.AuditLog;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({"SYSTEM_ADMIN", "LOGISTICS_COORDINATOR", "WAREHOUSE_MANAGER", "CUSTOMS_AGENT", "VENDOR_REP"})
@Stateless
public class AuditLogWriter {

    @PersistenceContext(unitName = "supplyChainPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void write(AuditLog log) {
        em.persist(log);
    }
}
