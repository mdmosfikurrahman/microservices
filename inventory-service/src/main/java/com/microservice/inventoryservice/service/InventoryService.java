package com.microservice.inventoryservice.service;

public interface InventoryService {
    boolean isInStock(String skuCode);
}
