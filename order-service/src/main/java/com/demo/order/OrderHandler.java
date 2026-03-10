package com.demo.order;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP handler for order endpoints: POST /orders and GET /orders/{id}.
 */
public class OrderHandler implements HttpHandler {

    private final OrderService orderService;

    public OrderHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("POST") && path.equals("/orders")) {
                handleCreateOrder(exchange);
            } else if (method.equals("GET") && path.startsWith("/orders/")) {
                handleGetOrder(exchange, path);
            } else {
                sendResponse(exchange, 405, errorJson("Method not allowed"));
            }
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            sendResponse(exchange, 500, errorJson("Internal server error"));
        }
    }

    private void handleCreateOrder(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody()))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            Map<String, Object> requestBody = JsonUtil.parseObject(body.toString());
            String itemsJson = (String) requestBody.get("items");
            List<Map<String, Object>> items = JsonUtil.parseArray(itemsJson);

            Order order = orderService.createOrder(items);
            Map<String, Object> responseMap = OrderService.orderToMap(order);
            sendResponse(exchange, 201, JsonUtil.toJson(responseMap));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, errorJson(e.getMessage()));
        }
    }

    private void handleGetOrder(HttpExchange exchange, String path) throws IOException {
        String orderId = path.substring("/orders/".length());
        Order order = orderService.getOrder(orderId);

        if (order == null) {
            sendResponse(exchange, 404, errorJson("Order not found"));
        } else {
            Map<String, Object> responseMap = OrderService.orderToMap(order);
            sendResponse(exchange, 200, JsonUtil.toJson(responseMap));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String errorJson(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        return JsonUtil.toJson(error);
    }
}
