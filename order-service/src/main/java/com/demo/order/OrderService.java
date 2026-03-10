package com.demo.order;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Business logic for creating and retrieving orders.
 * Calls the product service to validate products and fetch their details.
 */
public class OrderService {

    private final ProductClient productClient;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger orderCounter = new AtomicInteger(0);

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    /**
     * Creates a new order from a list of item requests (each containing productId and quantity).
     *
     * @param itemRequests list of maps with "productId" and "quantity" keys
     * @return the created Order
     * @throws IllegalArgumentException if items list is empty or a product is not found
     */
    public Order createOrder(List<Map<String, Object>> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> request : itemRequests) {
            int productId = ((Number) request.get("productId")).intValue();
            int quantity = ((Number) request.get("quantity")).intValue();

            Map<String, Object> product = productClient.getProduct(productId);
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + productId);
            }

            String name = (String) product.get("name");
            double price = ((Number) product.get("price")).doubleValue();

            items.add(new OrderItem(productId, name, price, quantity));
        }

        String orderId = "ORD-" + orderCounter.incrementAndGet();
        Order order = new Order(orderId, items);
        orders.put(orderId, order);
        return order;
    }

    /**
     * Retrieves an existing order by its ID.
     *
     * @return the Order, or null if not found
     */
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    /**
     * Converts an Order to a JSON-serializable map.
     */
    public static Map<String, Object> orderToMap(Order order) {
        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("subtotal", item.getSubtotal());
            itemMaps.add(itemMap);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("items", itemMaps);
        map.put("total", order.getTotal());
        return map;
    }
}
