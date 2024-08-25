package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.*;
import com.microservice.orderservice.model.Order;
import com.microservice.orderservice.model.OrderLineItems;
import com.microservice.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Override
    public void placeOrder(OrderRequest orderRequest) {
        Order order = mapToOrder(orderRequest);

        List<String> skuCodes = order.getOrderLineItems().stream().map(OrderLineItems::getSkuCode).toList();

        // Call inventory service and place order if product is in stock
        InventoryResponse[] inventoryResponses = webClient.get()
                .uri("http://localhost:8083/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponses).allMatch(inventoryResponse -> inventoryResponse::isInStock);

        if (allProductsInStock) {
            orderRepository.save(order);
            log.info("Order is saved for ID {}.", order.getId());
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later!");
        }
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private Order mapToOrder(OrderRequest orderRequest) {
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsRequests().stream()
                .map(this::mapToOrderLineItems)
                .collect(Collectors.toList());

        return Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .orderLineItems(orderLineItems)
                .build();
    }

    private OrderLineItems mapToOrderLineItems(OrderLineItemsRequest orderLineItemsRequest) {
        return OrderLineItems.builder()
                .skuCode(orderLineItemsRequest.getSkuCode())
                .price(orderLineItemsRequest.getPrice())
                .quantity(orderLineItemsRequest.getQuantity())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderLineItemsResponse> orderLineItemsResponses = order.getOrderLineItems().stream()
                .map(this::mapToOrderLineItemsResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .orderLineItems(orderLineItemsResponses)
                .build();
    }

    private OrderLineItemsResponse mapToOrderLineItemsResponse(OrderLineItems orderLineItems) {
        return OrderLineItemsResponse.builder()
                .skuCode(orderLineItems.getSkuCode())
                .price(orderLineItems.getPrice())
                .quantity(orderLineItems.getQuantity())
                .build();
    }
}
