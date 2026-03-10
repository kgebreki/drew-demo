package com.demo.order;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

/**
 * Main entry point for the Order Service. Starts an HTTP server on port 8082.
 */
public class OrderServer {

    private static final int DEFAULT_PORT = 8082;
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8081";

    public static void main(String[] args) throws Exception {
        ProductClient productClient = new ProductClient(PRODUCT_SERVICE_URL);
        OrderService orderService = new OrderService(productClient);
        OrderHandler orderHandler = new OrderHandler(orderService);

        HttpServer server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
        server.createContext("/orders", orderHandler);
        server.setExecutor(null);
        server.start();

        System.out.println("Order Service started on port " + DEFAULT_PORT);
    }
}
