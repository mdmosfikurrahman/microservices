package com.microservice.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.orderservice.dto.OrderLineItemsRequest;
import com.microservice.orderservice.dto.OrderRequest;
import com.microservice.orderservice.dto.OrderResponse;
import com.microservice.orderservice.model.Order;
import com.microservice.orderservice.model.OrderLineItems;
import com.microservice.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback(false)
class OrderServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrder() throws Exception {
        OrderLineItemsRequest orderLineItemsRequest = new OrderLineItemsRequest(null, "SKU123", 99.99, 2);
        OrderRequest orderRequest = new OrderRequest(List.of(orderLineItemsRequest));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderLineItems()).hasSize(1);
        assertThat(orders.get(0).getOrderLineItems().get(0).getSkuCode()).isEqualTo("SKU123");
    }

    @Test
    void testGetAllOrders() throws Exception {
        orderRepository.save(Order.builder()
                .orderNumber("TEST_ORDER_1")
                .orderLineItems(List.of(OrderLineItems.builder()
                        .skuCode("SKU123")
                        .price(99.99)
                        .quantity(2)
                        .build()))
                .build());

        String response = mockMvc.perform(MockMvcRequestBuilders.get("/api/order")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<OrderResponse> orderResponses = objectMapper.readValue(response, objectMapper.getTypeFactory().constructCollectionType(List.class, OrderResponse.class));
        assertThat(orderResponses).hasSize(1);
        assertThat(orderResponses.get(0).getOrderLineItems()).hasSize(1);
        assertThat(orderResponses.get(0).getOrderLineItems().get(0).getSkuCode()).isEqualTo("SKU123");
    }
}
