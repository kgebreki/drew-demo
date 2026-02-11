package com.demo.order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core business logic for order creation and retrieval.
 * Validates requests, coordinates with the product service, and stores orders in memory.
 */
public class OrderService {

    private final ProductClient productClient;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger orderCounter = new AtomicInteger(0);

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    /**
     * Creates a new order from request items.
     * Validates input, looks up each product, computes subtotals and total.
     *
     * @param requestItems items with only productId and quantity set
     * @return the fully populated Order
     * @throws IllegalArgumentException if items list is null or empty
     * @throws ProductClient.ProductNotFoundException if any product ID is invalid
     * @throws IOException if the product service call fails
     */
    public Order createOrder(List<OrderItem> requestItems) throws IOException {
        if (requestItems == null || requestItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        List<OrderItem> enrichedItems = new ArrayList<>();
        double total = 0.0;

        for (OrderItem item : requestItems) {
            Map<String, String> product = productClient.getProduct(item.getProductId());
            String name = product.get("name");
            double price = Double.parseDouble(product.get("price"));
            double subtotal = Math.round(price * item.getQuantity() * 100.0) / 100.0;

            enrichedItems.add(new OrderItem(
                    item.getProductId(), name, price, item.getQuantity(), subtotal));
            total += subtotal;
        }

        total = Math.round(total * 100.0) / 100.0;
        String orderId = "ORD-" + orderCounter.incrementAndGet();
        Order order = new Order(orderId, enrichedItems, total);
        orders.put(orderId, order);
        return order;
    }

    /**
     * Retrieves a previously created order by ID.
     *
     * @param orderId the order ID to look up
     * @return the Order, or null if not found
     */
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }
}
