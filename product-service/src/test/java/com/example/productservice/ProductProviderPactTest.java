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