package com.microservice.productservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.productservice.dto.ProductRequest;
import com.microservice.productservice.dto.ProductResponse;
import com.microservice.productservice.model.Product;
import com.microservice.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void testCreateProduct() throws Exception {
        ProductRequest productRequest = ProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated());

        List<Product> products = productRepository.findAll();
        assertTrue(products.stream().anyMatch(p -> "Test Product".equals(p.getName())
                && "Test Description".equals(p.getDescription())
                && 99.99 == p.getPrice()));
    }

    @Test
    void testGetAllProducts() throws Exception {
        Product product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .build();
        productRepository.save(product);

        String response = mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ProductResponse> productResponses = objectMapper.readValue(response, objectMapper.getTypeFactory().constructCollectionType(List.class, ProductResponse.class));
        assertTrue(productResponses.stream().anyMatch(pr -> "Test Product".equals(pr.getName())
                && "Test Description".equals(pr.getDescription())
                && 99.99 == pr.getPrice()));
    }
}
