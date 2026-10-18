package com.globaltrade.scm.service;

import com.globaltrade.scm.ejb.remote.VendorServiceRemote;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ejb.EJB;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ArquillianExtension.class)
public class SmokeTestIT {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "smoke-test.jar")
                .addPackages(true, "com.globaltrade.scm")
                .addAsManifestResource("META-INF/persistence.xml", "persistence.xml");
    }

    @EJB
    private VendorServiceRemote vendorService;

    @Test
    void injectedBeanIsNotNull() {
        assertNotNull(vendorService, "VendorServiceRemote should have been injected by the container");
    }

    @Test
    void canCallARemoteMethodWithoutAuthentication() {

        String result = vendorService.deleteVendorSecureCheck(-999L);
        assertNotNull(result, "deleteVendorSecureCheck() should return a non-null response even for a non-existent vendor ID");
    }
}