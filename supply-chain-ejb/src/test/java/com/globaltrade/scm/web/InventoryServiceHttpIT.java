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

public class InventoryServiceHttpIT {

    private static final String BASE_URL = "http://localhost:8080/supplychain/inventory-test";
    private static final String VENDOR_TEST_URL = "http://localhost:8080/supplychain/vendor-test";
    private static final Pattern CREATED_ID_PATTERN =
            Pattern.compile("Created test inventory item with ID: (\\d+)");
    // Matches the HTML vendor-test create response: <p>Created vendor with ID: 42</p>
    private static final Pattern CREATED_VENDOR_ID_PATTERN =
            Pattern.compile("Created vendor with ID: (\\d+)");

    private String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private HttpResponse<String> get(String queryString) throws Exception {
        return getUrl(BASE_URL + queryString);
    }

    // (creating and suspending a vendor before testing inventory's rejection of it).
    private HttpResponse<String> getUrl(String fullUrl) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", basicAuthHeader("admin1", "Admin@123"))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Creates a fresh vendor via /vendor-test (it creates one unconditionally on every
    // default hit), extracts its ID, then suspends it via the new ?updateStatus= branch.
    // Returns the ID of a vendor that is now genuinely SUSPENDED in the database.
    private long createSuspendedVendor() throws Exception {
        HttpResponse<String> createResponse = getUrl(VENDOR_TEST_URL);
        assertEquals(200, createResponse.statusCode(), "Vendor creation (setup step) should succeed");

        Matcher matcher = CREATED_VENDOR_ID_PATTERN.matcher(createResponse.body());
        assertTrue(matcher.find(), "vendor-test response should contain 'Created vendor with ID: <n>'");
        long vendorId = Long.parseLong(matcher.group(1));

        HttpResponse<String> suspendResponse =
                getUrl(VENDOR_TEST_URL + "?updateStatus=" + vendorId + "&status=SUSPENDED");
        assertEquals(200, suspendResponse.statusCode(), "Suspending the freshly created vendor should succeed");
        assertTrue(suspendResponse.body().contains("Updated vendor " + vendorId + " status to SUSPENDED"));

        return vendorId;
    }

    // Creates a fresh item (vendor 1, which is ACTIVE by default) and returns its ID,
    // parsed straight out of the servlet's plain-text response.
    private long createTestItem() throws Exception {
        HttpResponse<String> response = get("");
        assertEquals(200, response.statusCode(), "Create should succeed against the default ACTIVE vendor");

        Matcher matcher = CREATED_ID_PATTERN.matcher(response.body());
        assertTrue(matcher.find(), "Response should contain 'Created test inventory item with ID: <n>'");
        return Long.parseLong(matcher.group(1));
    }

    @Test
    void createItemAgainstActiveVendorSucceeds() throws Exception {
        HttpResponse<String> response = get("");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Created test inventory item with ID:"));
        assertTrue(response.body().contains("for vendor 1"));
    }

    @Test
    void deleteNonExistentItemReturns404WithVendorNotFoundStyleMessage() throws Exception {
        HttpResponse<String> response = get("?delete=999999");

        assertEquals(404, response.statusCode(),
                "Deleting a non-existent item should surface InventoryItemNotFoundException as 404");
        assertTrue(response.body().contains("Delete failed:"));
        assertTrue(response.body().contains("Inventory item not found: 999999"));
    }

    @Test
    void deleteExistingItemSucceeds() throws Exception {
        long itemId = createTestItem();

        HttpResponse<String> response = get("?delete=" + itemId);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Deleted inventory item with ID: " + itemId));
    }

    @Test
    void adjustingBeyondAvailableQuantityReturns409() throws Exception {
        // createTestItem() always creates with quantity_on_hand = 100 (hardcoded in the servlet),
        // so requesting a withdrawal of 500 reliably triggers InsufficientInventoryException.
        long itemId = createTestItem();

        HttpResponse<String> response = get("?adjust=" + itemId + "&delta=-500");

        assertEquals(409, response.statusCode(),
                "Requesting more stock than is on hand should surface InsufficientInventoryException as 409");
        assertTrue(response.body().contains("Adjust failed:"));
        assertTrue(response.body().contains("Insufficient inventory"));
    }

    @Test
    void adjustingNonExistentItemReturns404() throws Exception {
        HttpResponse<String> response = get("?adjust=999999&delta=-1");

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Adjust failed:"));
        assertTrue(response.body().contains("Inventory item not found: 999999"));
    }

    @Test
    void creatingItemForSuspendedVendorReturns409() throws Exception {
        long suspendedVendorId = createSuspendedVendor();

        HttpResponse<String> response = get("?vendorId=" + suspendedVendorId);

        assertEquals(409, response.statusCode(),
                "Creating an inventory item for a SUSPENDED vendor should surface InvalidVendorStatusException as 409");
        assertTrue(response.body().contains("Create failed:"));
        assertTrue(response.body().contains("status SUSPENDED"));
    }
}