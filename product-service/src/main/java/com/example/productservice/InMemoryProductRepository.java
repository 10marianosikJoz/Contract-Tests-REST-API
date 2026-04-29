package com.example.productservice;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryProductRepository {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();

    Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    void addProduct(Product product) {
        products.put(product.id(), product);
    }

    void deleteProductById(Long id) {
        products.remove(id);
    }
}