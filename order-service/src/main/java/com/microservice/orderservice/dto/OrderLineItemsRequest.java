package com.microservice.orderservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemsRequest {

    private Long id;
    private String skuCode;
    private Double price;
    private Integer quantity;

}
