package com.coffee.ordering.system.application.service.impl;

import com.coffee.ordering.system.application.model.CreateOrderRequest;
import com.coffee.ordering.system.application.service.OrderValidationService;
import com.coffee.ordering.system.dataaccess.CustomerRepository;
import com.coffee.ordering.system.dto.CustomerDTO;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.dto.OrderItemDTO;
import com.coffee.ordering.system.dto.ProductDTO;
import com.coffee.ordering.system.exception.OrderException;
import com.coffee.ordering.system.mappers.OrderMapper;
import com.coffee.ordering.system.shop.v1.api.CoffeeShopApi;
import com.coffee.ordering.system.shop.v1.model.CoffeeShop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderValidationServiceImpl implements OrderValidationService {
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;
    private final CoffeeShopApi coffeeShopApi;

    @Override
    public CustomerDTO validateCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .map(orderMapper::buidCustomerDTO)
                .orElseThrow(() -> new OrderException("Could not find customer with customer id: " + customerId));
    }

    @Override
    public CoffeeShop validateCoffeeShop(CreateOrderRequest createOrderRequest) {
        try {
            return coffeeShopApi.getShopById(createOrderRequest.getCoffeeShopId());
        } catch (Exception e) {
            log.warn("Could not find Coffee Shop with id: {}", createOrderRequest.getCoffeeShopId());
            throw new OrderException("Could not find Coffee Shop with id: " + createOrderRequest.getCoffeeShopId());
        }
    }

    @Override
    public void validateOrder(OrderDTO order, CoffeeShop shop) {
        validateCoffeeShop(shop);
        updateOrderProductInformation(order, shop);
        validateInitialOrder(order);
        validateTotalPrice(order);
        validateItemsPrice(order);
    }

    private void validateCoffeeShop(CoffeeShop shop) {
        if (Boolean.FALSE.equals(shop.getActive())) {
            throw new OrderException("Coffee Shop with id " + shop.getShopId() + " is inactive.");
        }
    }

    private void updateOrderProductInformation(OrderDTO order, CoffeeShop shop) {
        order.getItems().forEach(orderItem ->
                shop.getProducts().forEach(shopProduct -> {
                    ProductDTO currentProduct = orderItem.getProduct();
                    if (currentProduct.getProductId().equals(shopProduct.getProductId())) {
                        currentProduct.alignWithShop(shopProduct.getName(),
                                shopProduct.getPrice());
                    }
                }));
    }

    private void validateInitialOrder(OrderDTO order) {
        if (order.getOrderStatus() != null || order.getOrderId() != null) {
            throw new OrderException("Order is not in correct state for initialization!");
        }
    }

    private void validateTotalPrice(OrderDTO order) {
        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderException("Total price must be greater than zero!");
        }
    }

    private void validateItemsPrice(OrderDTO order) {
        BigDecimal orderItemsTotal = order.getItems().stream().map(orderItem -> {
            validateItemPrice(orderItem);
            return orderItem.getSubTotal();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!order.getPrice().equals(orderItemsTotal)) {
            throw new OrderException("Total price: " + order.getPrice()
                    + " is not equal to Order items total: " + orderItemsTotal + "!");
        }
    }

    private void validateItemPrice(OrderItemDTO orderItem) {
        if (!isPriceValid(orderItem)) {
            throw new OrderException("Order item price: " + orderItem.getPrice() +
                    " is not valid for product " + orderItem.getProduct().getProductId());
        }
    }

    boolean isPriceValid(OrderItemDTO orderItem) {
        return orderItem.getPrice().compareTo(BigDecimal.ZERO) > 0 &&
                orderItem.getPrice().equals(orderItem.getProduct().getPrice()) &&
                orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())).equals(orderItem.getSubTotal());
    }
}
