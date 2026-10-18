package com.globaltrade.scm.interceptor;

import com.globaltrade.scm.entity.Vendor;
import com.globaltrade.scm.entity.VendorStatus;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.scm.ejb.exception.VendorNotFoundException;
import com.globaltrade.scm.ejb.exception.InvalidVendorStatusException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;


public class VendorValidationInterceptor {

    @PersistenceContext(unitName = "supplyChainPU")
    private EntityManager em;

    @AroundInvoke
    public Object validateVendor(InvocationContext ctx) throws Exception {
        Long vendorId = findAnnotatedVendorId(ctx);

        if (vendorId != null) {
            Vendor vendor = em.find(Vendor.class, vendorId);
            if (vendor == null) {
                throw new VendorNotFoundException(vendorId);
            }
            if (vendor.getStatus() == VendorStatus.SUSPENDED) {
                throw new InvalidVendorStatusException(vendorId, vendor.getStatus().name(),
                        "operation blocked by VendorValidationInterceptor (vendor is SUSPENDED)");
            }
        }

        return ctx.proceed();
    }


    private Long findAnnotatedVendorId(InvocationContext ctx) {
        Parameter[] parameters = ctx.getMethod().getParameters();
        Object[] args = ctx.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            for (Annotation annotation : parameters[i].getAnnotations()) {
                if (annotation.annotationType() == ValidateVendorActive.class) {
                    Object value = args[i];
                    return value instanceof Long ? (Long) value : null;
                }
            }
        }
        return null;
    }
}
