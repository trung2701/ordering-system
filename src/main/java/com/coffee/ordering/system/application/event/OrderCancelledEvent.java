package com.coffee.ordering.system.application.event;

import com.coffee.ordering.system.dto.OrderDTO;

import java.time.LocalDateTime;

public class OrderCancelledEvent extends AbstractOrderEvent {
    public OrderCancelledEvent(OrderDTO order,
                               LocalDateTime createdAt) {
        super(order, createdAt);
    }
}
