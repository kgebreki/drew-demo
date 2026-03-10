package com.demo.product;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * HTTP handler for product endpoints. Serves GET /products and GET /products/{id}.
 */
public class ProductHandler implements HttpHandler {

    private final ProductRepository repository;

    public ProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, JsonUtil.errorJson("Method not allowed"));
                return;
            }

            String path = exchange.getRequestURI().getPath();

            if ("/products".equals(path) || "/products/".equals(path)) {
                String json = JsonUtil.toJson(repository.findAll());
                sendResponse(exchange, 200, json);
            } else if (path.startsWith("/products/")) {
                String idSegment = path.substring("/products/".length());
                int id;
                try {
                    id = Integer.parseInt(idSegment);
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, JsonUtil.errorJson("Invalid product ID"));
                    return;
                }

                Optional<Product> product = repository.findById(id);
                if (product.isPresent()) {
                    sendResponse(exchange, 200, JsonUtil.toJson(product.get()));
                } else {
                    sendResponse(exchange, 404, JsonUtil.errorJson("Product not found"));
                }
            } else {
                sendResponse(exchange, 404, JsonUtil.errorJson("Not found"));
            }
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            sendResponse(exchange, 500, JsonUtil.errorJson("Internal server error"));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
