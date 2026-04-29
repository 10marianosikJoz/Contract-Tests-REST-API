package com.example.productservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class ProductModuleConfiguration {

    @Bean
    ProductFacade productService() {
        return new ProductFacade(new InMemoryProductRepository());
    }
}