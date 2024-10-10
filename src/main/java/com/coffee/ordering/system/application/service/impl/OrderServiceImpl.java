package com.coffee.ordering.system.application.service.impl;

import com.coffee.ordering.system.application.service.abstracts.AbstractOrderService;
import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderServiceImpl extends AbstractOrderService {

    protected void innerPay(OrderDTO order) {
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new OrderException("Order is not in correct state for pay operation!");
        }
        order.setOrderStatus(OrderStatus.PAID);
    }

    protected void innerApprove(OrderDTO order) {
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new OrderException("Order is not in correct state for approve operation!");
        }
        order.setOrderStatus(OrderStatus.APPROVED);
    }

    protected void innerInitCancel(OrderDTO order) {
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new OrderException("Order is not in correct state for initCancel operation!");
        }
        order.setOrderStatus(OrderStatus.CANCELLING);
    }

    protected void innerCancel(OrderDTO order) {
        if (!(order.getOrderStatus() == OrderStatus.CANCELLING || order.getOrderStatus() == OrderStatus.PENDING)) {
            throw new OrderException("Order is not in correct state for cancel operation!");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
    }
}
