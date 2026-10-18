package com.globaltrade.scm.web;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BmtRollbackHttpIT {

    private static final String BASE_URL = "http://localhost:8080/supplychain/vendor-test";

    private String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private HttpResponse<String> get(String queryString) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + queryString))
                .header("Authorization", basicAuthHeader("admin1", "Admin@123"))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void recalculatingNonExistentVendorRollsBackCleanly() throws Exception {
        HttpResponse<String> response = get("?recalcBMT=999999");

        assertEquals(200, response.statusCode(),
                "The bean handles this case explicitly and returns a message rather than throwing, so no error status is expected");
        assertTrue(response.body().contains("Vendor not found: 999999"),
                "Response should confirm the vendor lookup failed");
        assertTrue(response.body().contains("transaction rolled back"),
                "Response should confirm the BMT UserTransaction was rolled back, not left open or committed");
    }

    @Test
    void transactionDoesNotLeakAfterRollback() throws Exception {
        // First, force the rollback path.
        HttpResponse<String> rollbackResponse = get("?recalcBMT=999999");
        assertEquals(200, rollbackResponse.statusCode());
        assertTrue(rollbackResponse.body().contains("rolled back"));


        HttpResponse<String> normalResponse = get("?recalcBMT=1");
        assertEquals(200, normalResponse.statusCode(),
                "A normal recalculation immediately after a rollback should succeed without issue");
        assertTrue(normalResponse.body().contains("Vendor") ,
                "Should get a normal vendor-related result, not an error carried over from the prior rollback");
    }
}
