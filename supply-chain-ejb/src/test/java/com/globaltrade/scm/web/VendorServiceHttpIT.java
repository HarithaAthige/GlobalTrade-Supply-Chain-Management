package com.globaltrade.scm.web;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class VendorServiceHttpIT {

    private static final String BASE_URL = "http://localhost:8080/supplychain/vendor-test";

    private String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Test
    void vendorTestServletRunsFullSequenceAsAdmin() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", basicAuthHeader("admin1", "Admin@123"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
                "Expected 200 OK — if you see 401, check WildFly is running and admin1's credentials are correct");

        String body = response.body();

        assertTrue(body.contains("Created vendor with ID:"),
                "Response should confirm a new vendor was created (createVendor write path)");
        assertTrue(body.contains("Pacific Rim Freight Co"),
                "Newly created vendor should appear in the 'All Vendors' list");
        assertTrue(body.contains("BMT Recalculation Test:"),
                "Response should include the BMT (bean-managed transaction) recalculation section");
        assertTrue(body.contains("isCallerInRole() Test:"),
                "Response should include the programmatic-authorization (deleteVendorSecureCheck) section");
    }

    @Test
    void vendorTestServletRejectsUnauthenticatedRequest() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode(),
                "Unauthenticated requests should be challenged with 401 by the web-tier security constraint, " +
                        "never reaching the EJB layer at all");
    }
}
