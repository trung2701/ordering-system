package com.coffee.ordering.system.application.service;

import com.coffee.ordering.system.application.model.CreateOrderRequest;
import com.coffee.ordering.system.dto.CustomerDTO;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.shop.v1.model.CoffeeShop;

import java.util.UUID;

public interface OrderValidationService {
    CustomerDTO validateCustomer(UUID customerId);

    CoffeeShop validateCoffeeShop(CreateOrderRequest createOrderRequest);

    void validateOrder(OrderDTO order, CoffeeShop coffeeShop);
}
