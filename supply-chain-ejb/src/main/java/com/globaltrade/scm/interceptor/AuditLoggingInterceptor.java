package com.globaltrade.scm.interceptor;

import com.globaltrade.scm.entity.AuditLog;

import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditLoggingInterceptor {

    private static final Logger LOG = Logger.getLogger(AuditLoggingInterceptor.class.getName());

    @EJB
    private AuditLogWriter auditLogWriter;

    @Resource
    private SessionContext sessionContext;

    @AroundInvoke
    public Object logInvocation(InvocationContext ctx) throws Exception {
        long start = System.currentTimeMillis();
        String methodName = ctx.getMethod().getName();
        String entityType = ctx.getTarget().getClass().getSimpleName();

        try {
            Object result = ctx.proceed();
            recordAudit(entityType, methodName, "SUCCESS", start, null, getCallerName());
            return result;
        } catch (Exception e) {
            recordAudit(entityType, methodName, "FAILURE", start, e.getMessage(), getCallerName());
            throw e;
        }
    }

    private String getCallerName() {
        try {
            return sessionContext.getCallerPrincipal().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private void recordAudit(String entityType, String methodName, String action,
                             long startMillis, String errorDetail, String performedBy) {
        try {
            AuditLog log = new AuditLog();
            log.setEntityType(entityType);
            log.setAction(action);
            log.setMethodName(methodName);
            log.setPerformedBy(performedBy);
            log.setExecutionTimeMs(System.currentTimeMillis() - startMillis);
            log.setDetails(errorDetail);

            auditLogWriter.write(log);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to write audit log entry", e);
        }
    }
}
