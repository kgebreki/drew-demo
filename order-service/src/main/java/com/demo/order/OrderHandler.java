package com.demo.order;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles HTTP requests to /orders and /orders/{id}.
 * Routes POST for order creation and GET for order retrieval.
 */
public class OrderHandler implements HttpHandler {

    private final OrderService orderService;

    public OrderHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/orders".equals(path) && "POST".equals(method)) {
                handleCreateOrder(exchange);
            } else if (path.startsWith("/orders/") && "GET".equals(method)) {
                handleGetOrder(exchange);
            } else {
                sendResponse(exchange, 405, JsonUtil.errorToJson("Method not allowed"));
            }
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            sendResponse(exchange, 500, JsonUtil.errorToJson("Internal server error"));
        }
    }

    private void handleCreateOrder(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            List<Map<String, String>> parsed = JsonUtil.parseOrderRequest(body);

            List<OrderItem> requestItems = new ArrayList<>();
            for (Map<String, String> map : parsed) {
                int productId = Integer.parseInt(map.get("productId"));
                int quantity = Integer.parseInt(map.get("quantity"));
                requestItems.add(OrderItem.fromRequest(productId, quantity));
            }

            Order order = orderService.createOrder(requestItems);
            sendResponse(exchange, 201, JsonUtil.orderToJson(order));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, JsonUtil.errorToJson(e.getMessage()));
        } catch (ProductClient.ProductNotFoundException e) {
            sendResponse(exchange, 400, JsonUtil.errorToJson(e.getMessage()));
        } catch (IOException e) {
            System.err.println("Error calling product service: " + e.getMessage());
            sendResponse(exchange, 500, JsonUtil.errorToJson("Internal server error"));
        }
    }

    private void handleGetOrder(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String orderId = path.substring("/orders/".length());

        Order order = orderService.getOrder(orderId);
        if (order == null) {
            sendResponse(exchange, 404, JsonUtil.errorToJson("Order not found"));
        } else {
            sendResponse(exchange, 200, JsonUtil.orderToJson(order));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
