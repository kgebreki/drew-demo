package com.demo.order;

/**
 * Represents a single line item in an order, including product details and calculated subtotal.
 */
public class OrderItem {

    private final int productId;
    private final String name;
    private final double price;
    private final int quantity;
    private final double subtotal;

    public OrderItem(int productId, String name, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
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
