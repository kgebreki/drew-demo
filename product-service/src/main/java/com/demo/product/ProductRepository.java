package com.demo.product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory store for products, pre-loaded with the catalog.
 */
public class ProductRepository {

    private final Map<Integer, Product> products = new LinkedHashMap<>();

    public ProductRepository() {
        addProduct(new Product(1, "Laptop", 999.99));
        addProduct(new Product(2, "Mouse", 24.99));
        addProduct(new Product(3, "Keyboard", 74.99));
        addProduct(new Product(4, "Monitor", 349.99));
        addProduct(new Product(5, "Headphones", 149.99));
    }

    private void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    /**
     * Returns all products in insertion order.
     */
    public List<Product> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(products.values()));
    }

    /**
     * Returns a product by id, or empty if not found.
     */
    public Optional<Product> findById(int id) {
        return Optional.ofNullable(products.get(id));
    }
}
