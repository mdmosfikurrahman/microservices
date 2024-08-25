package com.microservice.orderservice.service;

import com.microservice.orderservice.dto.OrderRequest;
import com.microservice.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    void placeOrder(OrderRequest orderRequest);

    List<OrderResponse> getAllOrders();

}
