package com.example.orderservice;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

import java.util.Optional;

class ProductClient {

    private final WebClient webClient;

    ProductClient(WebClient webClient) {
        this.webClient = webClient;
    }

    Optional<Product> getProductById(Long id) {
        try {
            var product = webClient.get()
                    .uri("/api/products/{id}", id)
                    .retrieve()
                    .bodyToMono(Product.class)
                    .block();

            return Optional.ofNullable(product);

        } catch (NotFound e) {
            return Optional.empty();
        }
    }
}