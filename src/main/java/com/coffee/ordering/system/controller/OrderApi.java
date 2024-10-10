package com.coffee.ordering.system.controller;

import com.coffee.ordering.system.application.model.CreateOrderRequest;
import com.coffee.ordering.system.application.model.CreateOrderResponse;
import com.coffee.ordering.system.application.model.OrderDetailResponse;
import com.coffee.ordering.system.application.model.TrackOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(value = "/api/v1/orders", produces = "application/json")
public interface OrderApi {
    @PostMapping
    ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest createOrderRequest);

    @GetMapping("/status/{trackingId}")
    ResponseEntity<TrackOrderResponse> getOrderByTrackingId(@PathVariable UUID trackingId);

    @GetMapping("/{trackingId}")
    ResponseEntity<OrderDetailResponse> getOrderById(@PathVariable UUID trackingId);
}
