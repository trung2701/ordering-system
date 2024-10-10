package com.coffee.ordering.system.application.service.impl;

import com.coffee.ordering.system.application.event.OrderCreatedOutboxEvent;
import com.coffee.ordering.system.application.model.*;
import com.coffee.ordering.system.application.service.OrderApplicationService;
import com.coffee.ordering.system.application.service.OrderService;
import com.coffee.ordering.system.application.service.OrderValidationService;
import com.coffee.ordering.system.application.service.OutboxService;
import com.coffee.ordering.system.application.service.helper.OrderHelper;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.mappers.OrderMapper;
import com.coffee.ordering.system.shop.v1.api.CoffeeShopApi;
import com.coffee.ordering.system.shop.v1.model.CoffeeShop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;
import java.util.UUID;

import static com.coffee.ordering.system.common.CoffeeConstants.UTC;
import static java.time.LocalDateTime.now;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class OrderApplicationServiceImpl implements OrderApplicationService {

    private final OrderMapper orderMapper;

    private final OrderService orderService;
    private final OutboxService outboxService;
    private final OrderValidationService orderValidationService;
    private final OrderHelper orderHelper;
    private final CoffeeShopApi coffeeShopApi;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        orderValidationService.validateCustomer(createOrderRequest.getCustomerId());
        CoffeeShop coffeeShop = orderValidationService.validateCoffeeShop(createOrderRequest);
        OrderDTO orderDTO = orderMapper.buildOrderDTO(createOrderRequest);
        orderValidationService.validateOrder(orderDTO, coffeeShop);

        orderService.initializeOrder(orderDTO);
        orderHelper.saveOrder(orderDTO);
        log.info("Order is created with id: {}", orderDTO.getOrderId());

        OrderCreatedOutboxEvent orderCreatedOutboxEvent = new OrderCreatedOutboxEvent(orderDTO, now(ZoneId.of(UTC)));
        outboxService.createPaymentOrderOutbox(orderCreatedOutboxEvent);

        return orderMapper.buildCreateOrderResponse(orderCreatedOutboxEvent.getOrder(), "Order created successfully");
    }

    @Override
    public TrackOrderResponse trackOrder(TrackOrderRequest trackOrderRequest) {
        return orderHelper.trackOrder(trackOrderRequest);
    }

    @Override
    public OrderDetailResponse getOrderById(UUID trackingId) {
        return orderHelper.getOrderById(trackingId);
    }
}
