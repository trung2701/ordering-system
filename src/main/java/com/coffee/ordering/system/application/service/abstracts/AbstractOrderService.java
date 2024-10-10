package com.coffee.ordering.system.application.service.abstracts;

import com.coffee.ordering.system.application.event.OrderCancelledEvent;
import com.coffee.ordering.system.application.event.OrderPaidEvent;
import com.coffee.ordering.system.application.service.OrderService;
import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static com.coffee.ordering.system.common.CoffeeConstants.UTC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public abstract class AbstractOrderService implements OrderService {
    public void initializeOrder(OrderDTO order) {
        order.setOrderId(UUID.randomUUID());
        order.setTrackingId(UUID.randomUUID());
        order.setOrderStatus(OrderStatus.PENDING);
        order.getDeliveryAddress().setId(UUID.randomUUID());
        order.getItems().forEach(item -> {
            item.setOrderId(order.getOrderId());
            item.setOrderItemId(UUID.randomUUID());
        });
        log.info("Order with id: {} is initiated", order.getOrderId());
    }

    @Override
    public OrderPaidEvent payOrder(OrderDTO order) {
        innerPay(order);
        log.info("Order with id: {} is updated with paid status", order.getOrderId());
        return new OrderPaidEvent(order, LocalDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void approveOrder(OrderDTO order) {
        innerApprove(order);
        log.info("Order with id: {} is approved", order.getOrderId());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(OrderDTO order, List<String> failureMessages) {
        innerInitCancel(order);
        log.info("Order payment is cancelling for order id: {}", order.getOrderId());
        updateFailureMessages(order, failureMessages);
        return new OrderCancelledEvent(order, LocalDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void cancelOrder(OrderDTO order, List<String> failureMessages) {
        innerCancel(order);
        log.info("Order with id: {} is cancelled", order.getOrderId());
        updateFailureMessages(order, failureMessages);
    }

    protected abstract void innerPay(OrderDTO order);

    protected abstract void innerApprove(OrderDTO order);

    protected abstract void innerInitCancel(OrderDTO order);

    protected abstract void innerCancel(OrderDTO order);

    private void updateFailureMessages(OrderDTO order, List<String> failureMessages) {
        if (nonNull(order.getFailureMessages()) && nonNull(failureMessages)) {
            order.getFailureMessages().addAll(failureMessages.stream().filter(message -> !message.isEmpty()).toList());
        } else if (isNull(order.getFailureMessages())) {
            order.setFailureMessages(failureMessages);
        }
    }
}
