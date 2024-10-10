package com.coffee.ordering.system.application.event;

import com.coffee.ordering.system.dto.OrderDTO;

import java.time.LocalDateTime;

public class OrderPaidEvent extends AbstractOrderEvent {
    public OrderPaidEvent(OrderDTO order,
                          LocalDateTime createdAt) {
        super(order, createdAt);
    }
}
