package com.coffee.ordering.system.application.service;

import com.coffee.ordering.system.application.event.OrderCancelledEvent;
import com.coffee.ordering.system.application.event.OrderPaidEvent;
import com.coffee.ordering.system.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    void initializeOrder(OrderDTO order);

    OrderPaidEvent payOrder(OrderDTO order);

    void approveOrder(OrderDTO order);

    OrderCancelledEvent cancelOrderPayment(OrderDTO order, List<String> failureMessages);

    void cancelOrder(OrderDTO order, List<String> failureMessages);
}
