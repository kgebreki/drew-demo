package com.demo.order;

/**
 * Represents a single line item in an order.
 * Immutable — enriched instances are created after product lookup.
 */
public class OrderItem {

    private final int productId;
    private final String name;
    private final double price;
    private final int quantity;
    private final double subtotal;

    public OrderItem(int productId, String name, double price, int quantity, double subtotal) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    /**
     * Creates a request-only item with just productId and quantity.
     */
    public static OrderItem fromRequest(int productId, int quantity) {
        return new OrderItem(productId, null, 0.0, quantity, 0.0);
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
