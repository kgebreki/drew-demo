package com.demo.order;

import java.util.List;

/**
 * Represents a complete order with its ID, line items, and total.
 * Immutable — items list is defensively copied.
 */
public class Order {

    private final String orderId;
    private final List<OrderItem> items;
    private final double total;

    public Order(String orderId, List<OrderItem> items, double total) {
        this.orderId = orderId;
        this.items = List.copyOf(items);
        this.total = total;
    }

    public String getOrderId() {
        return orderId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }
}
