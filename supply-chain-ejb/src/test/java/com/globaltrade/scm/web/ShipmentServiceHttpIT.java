package com.globaltrade.scm.web;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ShipmentServiceHttpIT {

    private static final String BASE_URL = "http://localhost:8080/supplychain/shipment-test";
    private static final Pattern CREATED_ID_PATTERN =
            Pattern.compile("Created test shipment with ID: (\\d+)");

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

    // Creates a fresh shipment (against vendor 1, ACTIVE) and returns its ID.
    private long createTestShipment() throws Exception {
        HttpResponse<String> response = get("");
        assertEquals(200, response.statusCode(), "Create should succeed against the default ACTIVE vendor");

        Matcher matcher = CREATED_ID_PATTERN.matcher(response.body());
        assertTrue(matcher.find(), "Response should contain 'Created test shipment with ID: <n>'");
        return Long.parseLong(matcher.group(1));
    }

    @Test
    void createShipmentAgainstActiveVendorSucceeds() throws Exception {
        HttpResponse<String> response = get("");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Created test shipment with ID:"));
        assertTrue(response.body().contains("Scheduled customs deadline timer"));
    }

    @Test
    void deleteNonExistentShipmentReturns404() throws Exception {
        HttpResponse<String> response = get("?delete=999999");

        assertEquals(404, response.statusCode(),
                "Deleting a non-existent shipment should surface ShipmentNotFoundException as 404");
        assertTrue(response.body().contains("Delete failed:"));
        assertTrue(response.body().contains("Shipment not found: 999999"));
    }

    @Test
    void deleteExistingShipmentSucceeds() throws Exception {
        long shipmentId = createTestShipment();

        HttpResponse<String> response = get("?delete=" + shipmentId);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Deleted shipment with ID: " + shipmentId));
        assertTrue(response.body().contains("Cancelled customs deadline timer"));
    }
}
