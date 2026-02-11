package com.demo.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal hand-rolled JSON serializer and deserializer.
 * Handles flat objects and arrays of flat objects.
 */
public class JsonUtil {

    private JsonUtil() {
    }

    /**
     * Parses a flat JSON object into a map of string key-value pairs.
     * Values are stored as raw strings (caller converts to int/double as needed).
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new HashMap<>();
        String trimmed = json.trim();
        if (trimmed.startsWith("{")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("}")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        trimmed = trimmed.trim();
        if (trimmed.isEmpty()) {
            return map;
        }

        List<String> pairs = splitByComma(trimmed);
        for (String pair : pairs) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex == -1) {
                continue;
            }
            String key = pair.substring(0, colonIndex).trim();
            String value = pair.substring(colonIndex + 1).trim();

            // Strip quotes from key
            key = stripQuotes(key);
            // Strip quotes from string values
            value = stripQuotes(value);

            map.put(key, value);
        }
        return map;
    }

    /**
     * Parses a JSON array of flat objects into a list of maps.
     */
    public static List<Map<String, String>> parseArray(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        String trimmed = json.trim();
        if (trimmed.startsWith("[")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("]")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        trimmed = trimmed.trim();
        if (trimmed.isEmpty()) {
            return result;
        }

        // Split by top-level object boundaries
        List<String> objects = splitObjects(trimmed);
        for (String obj : objects) {
            result.add(parseObject(obj.trim()));
        }
        return result;
    }

    /**
     * Extracts the items array from a POST /orders request body and parses it.
     */
    public static List<Map<String, String>> parseOrderRequest(String json) {
        String trimmed = json.trim();
        int bracketStart = trimmed.indexOf('[');
        int bracketEnd = trimmed.lastIndexOf(']');
        if (bracketStart == -1 || bracketEnd == -1 || bracketEnd <= bracketStart) {
            throw new IllegalArgumentException("Invalid request body");
        }
        String arrayContent = trimmed.substring(bracketStart, bracketEnd + 1);
        return parseArray(arrayContent);
    }

    /**
     * Serializes an Order to a JSON string.
     */
    public static String orderToJson(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"orderId\":\"").append(order.getOrderId()).append("\",\"items\":[");

        List<OrderItem> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(orderItemToJson(items.get(i)));
        }

        sb.append("],\"total\":").append(formatPrice(order.getTotal())).append("}");
        return sb.toString();
    }

    /**
     * Serializes an OrderItem to a JSON string.
     */
    public static String orderItemToJson(OrderItem item) {
        return "{\"productId\":" + item.getProductId()
                + ",\"name\":\"" + item.getName() + "\""
                + ",\"price\":" + formatPrice(item.getPrice())
                + ",\"quantity\":" + item.getQuantity()
                + ",\"subtotal\":" + formatPrice(item.getSubtotal()) + "}";
    }

    /**
     * Serializes an error message to a JSON string.
     */
    public static String errorToJson(String message) {
        return "{\"error\":\"" + message + "\"}";
    }

    /**
     * Formats a double price to avoid floating-point display artifacts.
     */
    private static String formatPrice(double price) {
        return BigDecimal.valueOf(price)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Splits a string by commas, but not commas inside quoted strings.
     */
    private static List<String> splitByComma(String str) {
        List<String> parts = new ArrayList<>();
        boolean inQuotes = false;
        int start = 0;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"' && (i == 0 || str.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(str.substring(start, i));
                start = i + 1;
            }
        }
        if (start < str.length()) {
            parts.add(str.substring(start));
        }
        return parts;
    }

    /**
     * Splits a string containing multiple JSON objects by their boundaries.
     * Handles nested braces by tracking depth.
     */
    private static List<String> splitObjects(String str) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    objects.add(str.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }
}
