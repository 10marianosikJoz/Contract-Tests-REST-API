package com.example.orderservice;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "product-service", pactVersion = PactSpecVersion.V4)
class ProductClientPactTest {

    @Pact(consumer = "order-service", provider = "product-service")
    V4Pact getExistingProduct(PactDslWithProvider builder) {
        return builder
                .given("product with id 1 exists")
                .uponReceiving("a request for product with id 1")
                .path("/api/products/1")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .numberType("id", 1L)
                        .stringType("name", "Laptop")
                        .decimalType("price", 2999.99))
                .toPact(V4Pact.class);
    }

    @Pact(consumer = "order-service", provider = "product-service")
    V4Pact getNotFoundProduct(PactDslWithProvider builder) {
        return builder
                .given("product with id 999 does not exist")
                .uponReceiving("a request for non-existing product")
                .path("/api/products/999")
                .method("GET")
                .willRespondWith()
                .status(404)
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "getExistingProduct")
    void shouldReturnProductWhenProductExists(MockServer mockServer) {
        var client = new ProductClient(WebClient.builder().baseUrl(mockServer.getUrl()).build());

        var result = client.getProductById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
    }

    @Test
    @PactTestFor(pactMethod = "getNotFoundProduct")
    void shouldReturnEmptyWhenProductNotFound(MockServer mockServer) {
        var client = new ProductClient(WebClient.builder().baseUrl(mockServer.getUrl()).build());

        var result = client.getProductById(999L);

        assertThat(result).isEmpty();
    }
}