package com.globaltrade.scm.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PerformanceMonitoringInterceptor {

    private static final Logger LOGGER = Logger.getLogger(PerformanceMonitoringInterceptor.class.getName());
    private static final long SLOW_THRESHOLD_MS = 500;

    @AroundInvoke
    public Object monitorPerformance(InvocationContext ctx) throws Exception {
        long start = System.currentTimeMillis();
        try {
            return ctx.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > SLOW_THRESHOLD_MS) {
                LOGGER.log(Level.WARNING,
                        "SLOW CALL: {0}.{1} took {2}ms (threshold {3}ms)",
                        new Object[]{
                                ctx.getTarget().getClass().getSimpleName(),
                                ctx.getMethod().getName(),
                                elapsed,
                                SLOW_THRESHOLD_MS
                        });
            }
        }
    }
}
