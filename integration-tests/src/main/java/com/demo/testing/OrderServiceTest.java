package com.demo.testing;

import com.demo.testing.HttpTestClient.Response;

/**
 * Integration tests for the order service endpoints.
 */
public class OrderServiceTest {

    private static final String BASE_URL = "http://localhost:8082";

    private OrderServiceTest() {
    }

    /**
     * Runs all order service tests and returns the number of failures.
     */
    public static int runAll() {
        int failures = 0;
        failures += run("POST /orders — valid order with multiple items",
                OrderServiceTest::testCreateValidOrder);
        failures += run("POST /orders — invalid product ID returns 400",
                OrderServiceTest::testCreateOrderInvalidProduct);
        failures += run("POST /orders — empty items list returns 400",
                OrderServiceTest::testCreateOrderEmptyItems);
        failures += run("GET /orders/{id} — retrieve previously created order",
                OrderServiceTest::testGetExistingOrder);
        failures += run("GET /orders/ORD-999 — returns 404",
                OrderServiceTest::testGetOrderNotFound);
        return failures;
    }

    private static int run(String name, TestCase test) {
        try {
            String error = test.execute();
            if (error == null) {
                System.out.println("  PASS: " + name);
                return 0;
            } else {
                System.out.println("  FAIL: " + name + " — " + error);
                return 1;
            }
        } catch (Exception e) {
            System.out.println("  FAIL: " + name + " — Exception: " + e.getMessage());
            return 1;
        }
    }

    private static String testCreateValidOrder() throws Exception {
        String requestBody = "{\"items\":[{\"productId\":1,\"quantity\":2},{\"productId\":3,\"quantity\":1}]}";
        Response response = HttpTestClient.post(BASE_URL + "/orders", requestBody);

        if (response.getStatusCode() != 201) {
            return "Expected status 201, got " + response.getStatusCode() + " body: " + response.getBody();
        }

        String body = response.getBody();
        if (!body.contains("orderId")) {
            return "Response missing orderId";
        }
        if (!body.contains("Laptop")) {
            return "Response missing product name 'Laptop'";
        }
        if (!body.contains("Keyboard")) {
            return "Response missing product name 'Keyboard'";
        }
        // Laptop: 999.99 * 2 = 1999.98
        if (!body.contains("1999.98")) {
            return "Expected subtotal 1999.98 for Laptop, got: " + body;
        }
        // Total: 1999.98 + 74.99 = 2074.97
        if (!body.contains("2074.97")) {
            return "Expected total 2074.97, got: " + body;
        }

        return null;
    }

    private static String testCreateOrderInvalidProduct() throws Exception {
        String requestBody = "{\"items\":[{\"productId\":999,\"quantity\":1}]}";
        Response response = HttpTestClient.post(BASE_URL + "/orders", requestBody);

        if (response.getStatusCode() != 400) {
            return "Expected status 400, got " + response.getStatusCode();
        }

        String body = response.getBody();
        if (!body.contains("Product not found")) {
            return "Expected 'Product not found' in error, got: " + body;
        }

        return null;
    }

    private static String testCreateOrderEmptyItems() throws Exception {
        String requestBody = "{\"items\":[]}";
        Response response = HttpTestClient.post(BASE_URL + "/orders", requestBody);

        if (response.getStatusCode() != 400) {
            return "Expected status 400, got " + response.getStatusCode();
        }

        String body = response.getBody();
        if (!body.contains("Order must contain at least one item")) {
            return "Expected empty items error message, got: " + body;
        }

        return null;
    }

    private static String testGetExistingOrder() throws Exception {
        // Create an order first
        String requestBody = "{\"items\":[{\"productId\":2,\"quantity\":3}]}";
        Response createResponse = HttpTestClient.post(BASE_URL + "/orders", requestBody);

        if (createResponse.getStatusCode() != 201) {
            return "Setup failed: could not create order, status " + createResponse.getStatusCode();
        }

        // Extract orderId from the response
        String orderId = extractValue(createResponse.getBody(), "orderId");
        if (orderId == null) {
            return "Could not extract orderId from response: " + createResponse.getBody();
        }

        // Retrieve the order by ID
        Response getResponse = HttpTestClient.get(BASE_URL + "/orders/" + orderId);

        if (getResponse.getStatusCode() != 200) {
            return "Expected status 200, got " + getResponse.getStatusCode();
        }

        String body = getResponse.getBody();
        if (!body.contains(orderId)) {
            return "Retrieved order missing orderId " + orderId;
        }
        if (!body.contains("Mouse")) {
            return "Retrieved order missing product name 'Mouse'";
        }

        return null;
    }

    private static String testGetOrderNotFound() throws Exception {
        Response response = HttpTestClient.get(BASE_URL + "/orders/ORD-999");

        if (response.getStatusCode() != 404) {
            return "Expected status 404, got " + response.getStatusCode();
        }

        String body = response.getBody();
        if (!body.contains("Order not found")) {
            return "Expected 'Order not found' error, got: " + body;
        }

        return null;
    }

    /**
     * Extracts a quoted string value for a given key from a JSON string.
     */
    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            return null;
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        return json.substring(start, end);
    }

    @FunctionalInterface
    interface TestCase {
        String execute() throws Exception;
    }
}
