package com.globaltrade.scm.web;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecurityRoleHttpIT {

    private String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private HttpResponse<String> getAsVendorRep(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", basicAuthHeader("vendorrep1", "Vendor@123"))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void vendorRepIsBlockedFromVendorTest() throws Exception {
        HttpResponse<String> response =
                getAsVendorRep("http://localhost:8080/supplychain/vendor-test");

        assertEquals(403, response.statusCode(),
                "vendorrep1 is authenticated but not in an authorized role — expect 403, not 401");
    }

    @Test
    void vendorRepIsBlockedFromInventoryTest() throws Exception {
        HttpResponse<String> response =
                getAsVendorRep("http://localhost:8080/supplychain/inventory-test");

        assertEquals(403, response.statusCode());
    }

    @Test
    void vendorRepIsBlockedFromShipmentTest() throws Exception {
        HttpResponse<String> response =
                getAsVendorRep("http://localhost:8080/supplychain/shipment-test");

        assertEquals(403, response.statusCode());
    }
}