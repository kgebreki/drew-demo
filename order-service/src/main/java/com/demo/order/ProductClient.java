package com.demo.order;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP client that calls the product service to look up product details by ID.
 */
public class ProductClient {

    private final String baseUrl;

    public ProductClient() {
        this("http://localhost:8081");
    }

    public ProductClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Looks up a product by ID from the product service.
     *
     * @param productId the product ID to look up
     * @return a map with keys "id", "name", "price"
     * @throws ProductNotFoundException if the product service returns 404
     * @throws IOException if the HTTP call fails
     */
    public Map<String, String> getProduct(int productId) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)
                URI.create(baseUrl + "/products/" + productId).toURL().openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 404) {
                throw new ProductNotFoundException(productId);
            }
            if (responseCode != 200) {
                throw new IOException("Product service returned status " + responseCode);
            }

            String body = readResponseBody(connection);
            return JsonUtil.parseObject(body);
        } finally {
            connection.disconnect();
        }
    }

    private String readResponseBody(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /**
     * Thrown when the product service returns 404 for a product ID.
     */
    public static class ProductNotFoundException extends RuntimeException {

        private final int productId;

        public ProductNotFoundException(int productId) {
            super("Product not found: " + productId);
            this.productId = productId;
        }

        public int getProductId() {
            return productId;
        }
    }
}
