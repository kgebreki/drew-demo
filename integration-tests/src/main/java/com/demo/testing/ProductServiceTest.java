package com.demo.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for the Product Service endpoints.
 */
public class ProductServiceTest {

    private final HttpTestClient client;
    private final List<TestResult> results = new ArrayList<>();

    public ProductServiceTest(HttpTestClient client) {
        this.client = client;
    }

    /**
     * Runs all product service tests and returns results.
     */
    public List<TestResult> runAll() {
        testGetAllProducts();
        testGetProductById();
        testGetProductNotFound();
        return results;
    }

    private void testGetAllProducts() {
        String testName = "GET /products — returns all 5 products";
        try {
            HttpTestClient.Response response = client.get("/products");

            assertEqual(testName, 200, response.getStatusCode(), "status code");

            String body = response.getBody();
            // Verify all 5 product names are present
            assertContains(testName, body, "\"Laptop\"", "Laptop in response");
            assertContains(testName, body, "\"Mouse\"", "Mouse in response");
            assertContains(testName, body, "\"Keyboard\"", "Keyboard in response");
            assertContains(testName, body, "\"Monitor\"", "Monitor in response");
            assertContains(testName, body, "\"Headphones\"", "Headphones in response");

            // Verify it's an array (starts with [)
            if (!body.trim().startsWith("[")) {
                results.add(new TestResult(testName, false, "Expected JSON array, got: " + body.substring(0, Math.min(50, body.length()))));
                return;
            }

            results.add(new TestResult(testName, true, null));
        } catch (Exception e) {
            results.add(new TestResult(testName, false, e.getMessage()));
        }
    }

    private void testGetProductById() {
        String testName = "GET /products/1 — returns correct product";
        try {
            HttpTestClient.Response response = client.get("/products/1");

            assertEqual(testName, 200, response.getStatusCode(), "status code");

            String body = response.getBody();
            assertContains(testName, body, "\"id\":1", "id field");
            assertContains(testName, body, "\"Laptop\"", "product name");
            assertContains(testName, body, "999.99", "product price");

            results.add(new TestResult(testName, true, null));
        } catch (Exception e) {
            results.add(new TestResult(testName, false, e.getMessage()));
        }
    }

    private void testGetProductNotFound() {
        String testName = "GET /products/999 — returns 404";
        try {
            HttpTestClient.Response response = client.get("/products/999");

            assertEqual(testName, 404, response.getStatusCode(), "status code");

            String body = response.getBody();
            assertContains(testName, body, "\"error\"", "error field");

            results.add(new TestResult(testName, true, null));
        } catch (Exception e) {
            results.add(new TestResult(testName, false, e.getMessage()));
        }
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
