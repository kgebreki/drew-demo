package com.demo.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * Main test runner that executes all integration tests against the product and order services.
 * Both services must be running before executing this runner.
 */
public class TestRunner {

    private static final String PRODUCT_SERVICE_URL = "http://localhost:8081";
    private static final String ORDER_SERVICE_URL = "http://localhost:8082";

    public static void main(String[] args) {
        System.out.println("=== Integration Test Suite ===");
        System.out.println();

        // Verify services are reachable before running tests
        if (!waitForService(PRODUCT_SERVICE_URL + "/products", "Product Service")) {
            System.err.println("Product Service is not running on port 8081. Start it before running tests.");
            System.exit(1);
        }
        if (!waitForService(ORDER_SERVICE_URL + "/orders/ORD-0", "Order Service")) {
            System.err.println("Order Service is not running on port 8082. Start it before running tests.");
            System.exit(1);
        }

        List<TestResult> allResults = new ArrayList<>();

        // Run product service tests
        System.out.println("--- Product Service Tests ---");
        HttpTestClient productClient = new HttpTestClient(PRODUCT_SERVICE_URL);
        ProductServiceTest productTests = new ProductServiceTest(productClient);
        List<TestResult> productResults = productTests.runAll();
        printResults(productResults);
        allResults.addAll(productResults);

        System.out.println();

        // Run order service tests
        System.out.println("--- Order Service Tests ---");
        HttpTestClient orderClient = new HttpTestClient(ORDER_SERVICE_URL);
        OrderServiceTest orderTests = new OrderServiceTest(orderClient);
        List<TestResult> orderResults = orderTests.runAll();
        printResults(orderResults);
        allResults.addAll(orderResults);

        // Summary
        System.out.println();
        System.out.println("=== Summary ===");
        long passed = allResults.stream().filter(TestResult::isPassed).count();
        long failed = allResults.size() - passed;
        System.out.println("Total: " + allResults.size() + " | Passed: " + passed + " | Failed: " + failed);

        if (failed > 0) {
            System.out.println();
            System.out.println("FAILED TESTS:");
            for (TestResult result : allResults) {
                if (!result.isPassed()) {
                    System.out.println("  FAIL: " + result.getName());
                    System.out.println("        " + result.getErrorMessage());
                }
            }
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED");
            System.exit(0);
        }
    }

    private static void printResults(List<TestResult> results) {
        for (TestResult result : results) {
            if (result.isPassed()) {
                System.out.println("  PASS: " + result.getName());
            } else {
                System.out.println("  FAIL: " + result.getName());
                System.out.println("        " + result.getErrorMessage());
            }
        }
    }

    /**
     * Attempts to connect to a service, retrying a few times to allow for startup.
     */
    private static boolean waitForService(String url, String serviceName) {
        System.out.println("Checking " + serviceName + " at " + url + "...");
        for (int i = 0; i < 3; i++) {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.getResponseCode();
                conn.disconnect();
                System.out.println(serviceName + " is ready.");
                return true;
            } catch (Exception e) {
                if (i < 2) {
                    System.out.println(serviceName + " not ready, retrying in 1 second...");
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }
        }
        return false;
    }
}
