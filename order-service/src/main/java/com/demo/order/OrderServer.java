package com.demo.order;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Main entry point for the order service.
 * Starts an HTTP server on port 8082 and registers order handlers.
 */
public class OrderServer {

    private static final int DEFAULT_PORT = 8082;

    public static void main(String[] args) throws IOException {
        ProductClient productClient = new ProductClient();
        OrderService orderService = new OrderService(productClient);
        OrderHandler orderHandler = new OrderHandler(orderService);

        HttpServer server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
        server.createContext("/orders", orderHandler);
        server.setExecutor(null);
        server.start();

        System.out.println("Order Service started on port " + DEFAULT_PORT);
    }
}
