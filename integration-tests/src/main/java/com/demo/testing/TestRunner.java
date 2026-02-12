package com.demo.testing;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Main entry point for integration tests.
 * Starts both services as subprocesses, runs all tests, and reports results.
 */
public class TestRunner {

    private static final int MAX_STARTUP_WAIT_SECONDS = 60;

    public static void main(String[] args) {
        int exitCode = run();
        System.exit(exitCode);
    }

    private static int run() {
        Process productProcess = null;
        Process orderProcess = null;

        try {
            File projectRoot = findProjectRoot();

            System.out.println("Starting product service...");
            productProcess = startService(projectRoot, "product-service");

            System.out.println("Starting order service...");
            orderProcess = startService(projectRoot, "order-service");

            System.out.println("Waiting for product service on port 8081...");
            waitForService("http://localhost:8081/products");

            System.out.println("Waiting for order service on port 8082...");
            waitForService("http://localhost:8082/orders/health");

            System.out.println("Both services are up.\n");

            System.out.println("=== Product Service Tests ===");
            int failures = ProductServiceTest.runAll();

            System.out.println();
            System.out.println("=== Order Service Tests ===");
            failures += OrderServiceTest.runAll();

            System.out.println();
            if (failures == 0) {
                System.out.println("All tests PASSED!");
            } else {
                System.out.println(failures + " test(s) FAILED.");
            }
            return failures == 0 ? 0 : 1;

        } catch (Exception e) {
            System.err.println("Test runner failed: " + e.getMessage());
            e.printStackTrace(System.err);
            return 1;
        } finally {
            stopProcess(productProcess);
            stopProcess(orderProcess);
        }
    }

    /**
     * Locates the project root by looking for the directory containing
     * both pom.xml and the product-service subdirectory.
     */
    private static File findProjectRoot() {
        File dir = new File(System.getProperty("user.dir"));
        while (dir != null) {
            File pom = new File(dir, "pom.xml");
            File productDir = new File(dir, "product-service");
            if (pom.exists() && productDir.isDirectory()) {
                return dir;
            }
            dir = dir.getParentFile();
        }
        throw new RuntimeException("Could not find project root directory");
    }

    private static Process startService(File projectRoot, String module) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("mvn", "-pl", module, "compile", "exec:java");
        pb.directory(projectRoot);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        return pb.start();
    }

    /**
     * Polls the given URL until it returns any HTTP response, indicating the service is up.
     * Throws if the service doesn't respond within the timeout.
     */
    private static void waitForService(String url) throws Exception {
        long deadline = System.currentTimeMillis() + MAX_STARTUP_WAIT_SECONDS * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection conn = (HttpURLConnection)
                        URI.create(url).toURL().openConnection();
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(1000);
                conn.getResponseCode();
                conn.disconnect();
                return;
            } catch (IOException e) {
                Thread.sleep(500);
            }
        }
        throw new RuntimeException(
                "Service at " + url + " did not start within " + MAX_STARTUP_WAIT_SECONDS + " seconds");
    }

    private static void stopProcess(Process process) {
        if (process != null && process.isAlive()) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
        }
    }
}
