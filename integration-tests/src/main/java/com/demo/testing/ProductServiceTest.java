package com.demo.testing;

import com.demo.testing.HttpTestClient.Response;

/**
 * Integration tests for the product service endpoints.
 */
public class ProductServiceTest {

    private static final String BASE_URL = "http://localhost:8081";

    private ProductServiceTest() {
    }

    /**
     * Runs all product service tests and returns the number of failures.
     */
    public static int runAll() {
        int failures = 0;
        failures += run("GET /products — returns all 5 products",
                ProductServiceTest::testGetAllProducts);
        failures += run("GET /products/{id} — returns correct product",
                ProductServiceTest::testGetProductById);
        failures += run("GET /products/999 — returns 404",
                ProductServiceTest::testGetProductNotFound);
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

    private static String testGetAllProducts() throws Exception {
        Response response = HttpTestClient.get(BASE_URL + "/products");

        if (response.getStatusCode() != 200) {
            return "Expected status 200, got " + response.getStatusCode();
        }

        String body = response.getBody();
        if (!body.trim().startsWith("[")) {
            return "Response is not a JSON array";
        }

        String[] expectedNames = {"Laptop", "Mouse", "Keyboard", "Monitor", "Headphones"};
        for (String productName : expectedNames) {
            if (!body.contains(productName)) {
                return "Missing product: " + productName;
            }
        }

        return null;
    }

    private static String testGetProductById() throws Exception {
        Response response = HttpTestClient.get(BASE_URL + "/products/1");

        if (response.getStatusCode() != 200) {
            return "Expected status 200, got " + response.getStatusCode();
        }

        String body = response.getBody();
        if (!body.contains("Laptop")) {
            return "Expected product name 'Laptop', got: " + body;
        }
        if (!body.contains("999.99")) {
            return "Expected price 999.99, got: " + body;
        }

        return null;
    }

    private static String testGetProductNotFound() throws Exception {
        Response response = HttpTestClient.get(BASE_URL + "/products/999");

        if (response.getStatusCode() != 404) {
            return "Expected status 404, got " + response.getStatusCode();
        }

        String body = response.getBody();
        if (!body.contains("Product not found")) {
            return "Expected 'Product not found' in error response, got: " + body;
        }

        return null;
    }

    @FunctionalInterface
    interface TestCase {
        String execute() throws Exception;
    }
}
