package com.demo.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for the Order Service endpoints.
 */
public class OrderServiceTest {

    private final HttpTestClient client;
    private final List<TestResult> results = new ArrayList<>();

    public OrderServiceTest(HttpTestClient client) {
        this.client = client;
    }

    /**
     * Runs all order service tests and returns results.
     */
    public List<TestResult> runAll() {
        testCreateValidOrder();
        testCreateOrderInvalidProduct();
        testCreateOrderEmptyItems();
        testGetOrder();
        testGetOrderNotFound();
        return results;
    }

    private void testCreateValidOrder() {
        String testName = "POST /orders — valid order with multiple items, verify total";
        try {
            String body = "{\"items\":[{\"productId\":1,\"quantity\":2},{\"productId\":3,\"quantity\":1}]}";
            HttpTestClient.Response response = client.post("/orders", body);

            assertEqual(testName, 201, response.getStatusCode(), "status code");

            String responseBody = response.getBody();
            assertContains(testName, responseBody, "\"orderId\"", "orderId field");
            assertContains(testName, responseBody, "\"Laptop\"", "Laptop in items");
            assertContains(testName, responseBody, "\"Keyboard\"", "Keyboard in items");
            assertContains(testName, responseBody, "1999.98", "Laptop subtotal (999.99 * 2)");
            assertContains(testName, responseBody, "74.99", "Keyboard subtotal");
            assertContains(testName, responseBody, "2074.97", "order total");

            results.add(new TestResult(testName, true, null));
        } catch (Exception e) {
            results.add(new TestResult(testName, false, e.getMessage()));
        }
    }

    private void testCreateOrderInvalidProduct() {
        String testName = "POST /orders — invalid product ID, verify 400";
        try {
            String body = "{\"items\":[{\"productId\":999,\"quantity\":1}]}";
            HttpTestClient.Response response = client.post("/orders", body);

            assertEqual(testName, 400, response.getStatusCode(), "status code");

            String responseBody = response.getBody();
            assertContains(testName, responseBody, "\"error\"", "error field");
            assertContains(testName, responseBody, "999", "invalid product ID in error");

            results.add(new TestResult(testName, true, null));
        } catch (Exception e) {
            results.add(new TestResult(testName, false, e.getMessage()));
        }
    }

    private void testCreateOrderEmptyItems() {
        String testName = "POST /orders — empty items list, verify 400";
        try {
            String body = "{\"items\":[]}";
            HttpTestClient.Response response = client.post("/orders", body);

            assertEqual(testName, 400, response.getStatusCode(), "status code");

            String responseBody = response.getBody();
            assertContains(testName, responseBody, "\"error\"", "error field");
            assertContains(testName, responseBody, "at least one item", "empty items error message");

            results.add(new TestResult(testName, true, null));
        } catch (Exception e) {
            results.add(new TestResult(testName, false, e.getMessage()));
        }
    }

    private void testGetOrder() {
        String testName = "GET /orders/{id} — retrieve a previously created order";
        try {
            // First create an order
            String body = "{\"items\":[{\"productId\":2,\"quantity\":3}]}";
            HttpTestClient.Response createResponse = client.post("/orders", body);

            // Extract orderId from response
            String createBody = createResponse.getBody();
            String orderId = extractValue(createBody, "orderId");

            // Retrieve the order
            HttpTestClient.Response getResponse = client.get("/orders/" + orderId);

            assertEqual(testName, 200, getResponse.getStatusCode(), "status code");

            String getBody = getResponse.getBody();
            assertContains(testName, getBody, "\"orderId\":\"" + orderId + "\"", "matching orderId");
            assertContains(testName, getBody, "\"Mouse\"", "Mouse in items");
            assertContains(testName, getBody, "74.97", "subtotal (24.99 * 3)");

            results.add(new TestResult(testName, true, null));
        } catch (Exception e) {
            results.add(new TestResult(testName, false, e.getMessage()));
        }
    }

    private void testGetOrderNotFound() {
        String testName = "GET /orders/ORD-999 — returns 404";
        try {
            HttpTestClient.Response response = client.get("/orders/ORD-999");

            assertEqual(testName, 404, response.getStatusCode(), "status code");

            String responseBody = response.getBody();
            assertContains(testName, responseBody, "\"error\"", "error field");

            results.add(new TestResult(testName, true, null));
        } catch (Exception e) {
            results.add(new TestResult(testName, false, e.getMessage()));
        }
    }

    /**
     * Extracts a string value for a given key from a JSON string.
     */
    private String extractValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            throw new RuntimeException("Key '" + key + "' not found in: " + json);
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private void assertEqual(String testName, int expected, int actual, String field) {
        if (expected != actual) {
            throw new AssertionError("Expected " + field + " " + expected + " but got " + actual);
        }
    }

    private void assertContains(String testName, String body, String substring, String description) {
        if (!body.contains(substring)) {
            throw new AssertionError("Expected " + description + " (" + substring + ") in response: " + body);
        }
    }
}
