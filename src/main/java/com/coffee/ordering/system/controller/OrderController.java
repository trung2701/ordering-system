package com.coffee.ordering.system.controller;

import com.coffee.ordering.system.application.model.*;
import com.coffee.ordering.system.application.service.OrderApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderApplicationService orderApplicationService;

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        log.info("Creating order for customer: {} at Coffee Shop: {}", createOrderRequest.getCustomerId(),
                createOrderRequest.getCoffeeShopId());
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderRequest);
        log.info("Order created with tracking id: {}", createOrderResponse.getOrderTrackingId());
        return ResponseEntity.ok(createOrderResponse);
    }

    @Override
    public ResponseEntity<TrackOrderResponse> getOrderByTrackingId(@PathVariable UUID trackingId) {
        TrackOrderResponse trackOrderResponse = orderApplicationService.trackOrder(new TrackOrderRequest(trackingId));
        log.info("Returning order status with tracking id: {}", trackOrderResponse.getOrderTrackingId());
        return ResponseEntity.ok(trackOrderResponse);
    }

    @Override
    public ResponseEntity<OrderDetailResponse> getOrderById(UUID trackingId) {
        return ResponseEntity.ok(orderApplicationService.getOrderById(trackingId));
    }
}
