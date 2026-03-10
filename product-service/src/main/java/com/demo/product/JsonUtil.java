package com.demo.product;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Minimal hand-rolled JSON serialization for products and error responses.
 */
public class JsonUtil {

    private JsonUtil() {
        // Utility class — not instantiable
    }

    public static String toJson(Product product) {
        return "{\"id\":" + product.getId()
                + ",\"name\":\"" + escapeJson(product.getName())
                + "\",\"price\":" + product.getPrice() + "}";
    }

    public static String toJson(List<Product> products) {
        String items = products.stream()
                .map(JsonUtil::toJson)
                .collect(Collectors.joining(","));
        return "[" + items + "]";
    }

    public static String errorJson(String message) {
        return "{\"error\":\"" + escapeJson(message) + "\"}";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
