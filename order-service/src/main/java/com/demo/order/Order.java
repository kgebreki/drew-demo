package com.demo.order;

import java.util.Collections;
import java.util.List;

/**
 * Represents a customer order containing line items and a computed total.
 */
public class Order {

    private final String orderId;
    private final List<OrderItem> items;
    private final double total;

    public Order(String orderId, List<OrderItem> items) {
        this.orderId = orderId;
        this.items = List.copyOf(items);
        this.total = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
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
