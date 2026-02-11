package com.demo.product;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Main entry point for the Product Service. Starts an HTTP server on port 8081.
 */
public class ProductServer {

    private static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) throws IOException {
        ProductRepository repository = new ProductRepository();
        ProductHandler handler = new ProductHandler(repository);

        HttpServer server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
        server.createContext("/products", handler);
        server.setExecutor(null);
        server.start();

        System.out.println("Product Service running on port " + DEFAULT_PORT);
    }
}
