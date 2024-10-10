package com.coffee.ordering.system.application.service;

import com.coffee.ordering.system.application.model.*;
import jakarta.validation.Valid;

import java.util.UUID;

public interface OrderApplicationService {

    CreateOrderResponse createOrder(@Valid CreateOrderRequest createOrderRequest);

    TrackOrderResponse trackOrder(@Valid TrackOrderRequest trackOrderRequest);

    OrderDetailResponse getOrderById(UUID trackingId);
}
