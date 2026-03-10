package com.demo.product;

import java.util.List;
import java.util.Optional;

/**
 * In-memory store of products. Provides lookup by ID and listing of all products.
 */
public class ProductRepository {

    private final List<Product> products = List.of(
        new Product(1, "Laptop", 999.99),
        new Product(2, "Mouse", 24.99),
        new Product(3, "Keyboard", 74.99),
        new Product(4, "Monitor", 349.99),
        new Product(5, "Headphones", 149.99)
    );

    public List<Product> findAll() {
        return products;
    }

    public Optional<Product> findById(int id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }
}
