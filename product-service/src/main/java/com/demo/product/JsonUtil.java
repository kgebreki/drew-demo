package com.demo.product;

import java.util.List;

/**
 * Minimal hand-rolled JSON serialization for Product objects.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    /**
     * Serializes a single product to a JSON object string.
     */
    public static String toJson(Product product) {
        return "{ \"id\": " + product.getId()
                + ", \"name\": \"" + escapeJson(product.getName())
                + "\", \"price\": " + product.getPrice() + " }";
    }

    /**
     * Serializes a list of products to a JSON array string.
     */
    public static String toJson(List<Product> products) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(toJson(products.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Serializes a JSON error object with a single "error" key.
     */
    public static String errorJson(String message) {
        return "{ \"error\": \"" + escapeJson(message) + "\" }";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
