package com.coffee.ordering.system.application.event;

import com.coffee.ordering.system.dto.OrderDTO;

import java.time.LocalDateTime;

public class OrderCreatedOutboxEvent extends AbstractOrderEvent {
    public OrderCreatedOutboxEvent(OrderDTO order,
                                   LocalDateTime createdAt) {
        super(order, createdAt);
    }
}
