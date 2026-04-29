package com.example.orderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
class OrderModuleConfiguration {

    @Bean
    ProductClient productClient(final WebClient webClient) {
        return new ProductClient(webClient);
    }

    @Bean
    WebClient webClient(@Value("${product-service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}