package com.example.productservice;

import java.util.Optional;

class ProductFacade {

    private final InMemoryProductRepository inMemoryProductRepository;

    ProductFacade(final InMemoryProductRepository inMemoryProductRepository) {
        this.inMemoryProductRepository = inMemoryProductRepository;
    }

    Optional<Product> findById(Long id) {
        return inMemoryProductRepository.findById(id);
    }

    void addProduct(Product product) {
        inMemoryProductRepository.addProduct(product);
    }

    void removeProduct(Long id) {
        inMemoryProductRepository.deleteProductById(id);
    }
}