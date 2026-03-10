package com.demo.order;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal hand-rolled JSON serialization and parsing utility.
 * Handles flat objects and arrays of flat objects.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    /**
     * Serializes a map of key-value pairs into a JSON object string.
     */
    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializes a list of maps into a JSON array string.
     */
    public static String toJsonArray(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(toJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parses a JSON object string into a map of key-value pairs.
     */
    public static Map<String, Object> parseObject(String json) {
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON object");
        }
        json = json.substring(1, json.length() - 1).trim();
        Map<String, Object> map = new LinkedHashMap<>();
        if (json.isEmpty()) {
            return map;
        }

        int i = 0;
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;

            // Parse key
            if (json.charAt(i) != '"') {
                throw new IllegalArgumentException("Expected '\"' at position " + i);
            }
            int keyStart = i + 1;
            int keyEnd = json.indexOf('"', keyStart);
            String key = json.substring(keyStart, keyEnd);
            i = keyEnd + 1;

            // Skip colon
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (json.charAt(i) != ':') {
                throw new IllegalArgumentException("Expected ':' at position " + i);
            }
            i++;
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;

            // Parse value
            Object value;
            if (json.charAt(i) == '"') {
                int valStart = i + 1;
                int valEnd = json.indexOf('"', valStart);
                value = json.substring(valStart, valEnd);
                i = valEnd + 1;
            } else if (json.charAt(i) == '[') {
                int bracketCount = 1;
                int arrStart = i;
                i++;
                while (i < json.length() && bracketCount > 0) {
                    if (json.charAt(i) == '[') bracketCount++;
                    else if (json.charAt(i) == ']') bracketCount--;
                    i++;
                }
                value = json.substring(arrStart, i);
            } else if (json.charAt(i) == '{') {
                int braceCount = 1;
                int objStart = i;
                i++;
                while (i < json.length() && braceCount > 0) {
                    if (json.charAt(i) == '{') braceCount++;
                    else if (json.charAt(i) == '}') braceCount--;
                    i++;
                }
                value = json.substring(objStart, i);
            } else {
                int valStart = i;
                while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}') i++;
                String raw = json.substring(valStart, i).trim();
                if (raw.equals("null")) {
                    value = null;
                } else if (raw.equals("true")) {
                    value = true;
                } else if (raw.equals("false")) {
                    value = false;
                } else if (raw.contains(".")) {
                    value = Double.parseDouble(raw);
                } else {
                    value = Long.parseLong(raw);
                }
            }

            map.put(key, value);

            // Skip comma
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i < json.length() && json.charAt(i) == ',') i++;
        }

        return map;
    }

    /**
     * Parses a JSON array string into a list of maps.
     */
    public static List<Map<String, Object>> parseArray(String json) {
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IllegalArgumentException("Invalid JSON array");
        }
        json = json.substring(1, json.length() - 1).trim();
        List<Map<String, Object>> list = new ArrayList<>();
        if (json.isEmpty()) {
            return list;
        }

        int i = 0;
        while (i < json.length()) {
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length()) break;

            if (json.charAt(i) == '{') {
                int braceCount = 1;
                int objStart = i;
                i++;
                while (i < json.length() && braceCount > 0) {
                    if (json.charAt(i) == '{') braceCount++;
                    else if (json.charAt(i) == '}') braceCount--;
                    i++;
                }
                list.add(parseObject(json.substring(objStart, i)));
            }

            while (i < json.length() && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ',')) i++;
        }

        return list;
    }

    private static String valueToJson(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escape((String) value) + "\"";
        } else if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d) && d <= Long.MAX_VALUE && d >= Long.MIN_VALUE) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) value;
            return toJsonArray(list);
        }
        return "\"" + escape(value.toString()) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
