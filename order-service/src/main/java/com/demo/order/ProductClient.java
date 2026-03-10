package com.demo.order;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * HTTP client for communicating with the Product Service to look up product details.
 */
public class ProductClient {

    private final String baseUrl;

    public ProductClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Fetches a product by ID from the product service.
     *
     * @return a map of product fields, or null if the product was not found (404)
     * @throws RuntimeException if the HTTP call fails
     */
    public Map<String, Object> getProduct(int productId) {
        try {
            URL url = new URL(baseUrl + "/products/" + productId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status == 404) {
                conn.disconnect();
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
                return JsonUtil.parseObject(body.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch product " + productId + ": " + e.getMessage(), e);
        }
    }
}
