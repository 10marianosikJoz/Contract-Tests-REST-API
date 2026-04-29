package com.example.productservice;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * ============================================================
 *  PROVIDER SIDE - Pact Verification Test (product-service)
 * ============================================================
 *
 * Co się tutaj dzieje krok po kroku:
 *
 * 1. Pact wczytuje plik kontraktu JSON z @PactFolder
 *    (wygenerowany wcześniej przez order-service)
 * 2. Dla każdej interakcji w kontrakcie:
 *    a. Wywołuje metodę @State - ustawia wymagany stan serwisu
 *    b. Wysyła żądanie HTTP do naszego kontrolera (MockMvc)
 *    c. Porównuje odpowiedź z tym czego oczekuje kontrakt
 * 3. Jeśli odpowiedź nie pasuje do kontraktu → test FAIL
 **/

@SpringBootTest
@Provider("product-service")
@PactFolder("src/test/resources/pacts")
class ProductProviderPactTest {

    @Autowired
    private ProductController productController;

    @Autowired
    private ProductFacade productFacade;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        var mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        context.setTarget(new MockMvcTestTarget(mockMvc));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("product with id 1 exists")
    void productWithId1Exists() {
        productFacade.addProduct(Product.builder()
                .id(1L)
                .name("Laptop")
                .price(2999.99)
                .build());
    }

    @State("product with id 999 does not exist")
    void productWithId999DoesNotExist() {
        productFacade.removeProduct(999L);
    }
}