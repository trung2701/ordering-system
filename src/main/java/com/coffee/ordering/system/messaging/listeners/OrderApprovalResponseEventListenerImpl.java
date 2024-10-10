package com.coffee.ordering.system.messaging.listeners;

import com.coffee.ordering.system.application.communication.OrderApprovalResponseEventListener;
import com.coffee.ordering.system.application.model.OrderApprovalResponse;
import com.coffee.ordering.system.application.saga.OrderApprovalSaga;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.coffee.ordering.system.common.CoffeeConstants.FAILURE_MESSAGE_DELIMITER;

@Slf4j
@Validated
@Service
public class OrderApprovalResponseEventListenerImpl implements OrderApprovalResponseEventListener {

    private final OrderApprovalSaga orderApprovalSaga;

    public OrderApprovalResponseEventListenerImpl(OrderApprovalSaga orderApprovalSaga) {
        this.orderApprovalSaga = orderApprovalSaga;
    }

    @Override
    public void orderApproved(OrderApprovalResponse orderApprovalResponse) {
        orderApprovalSaga.process(orderApprovalResponse);
        log.info("Order Approval Saga completed with APPROVED status - order id: {}", orderApprovalResponse.getOrderId());
    }

    @Override
    public void orderRejected(OrderApprovalResponse orderApprovalResponse) {
        orderApprovalSaga.rollback(orderApprovalResponse);
        log.info("Order Approval Saga rollback operation is completed for order id: {} with failure messages: {}",
                orderApprovalResponse.getOrderId(),
                String.join(FAILURE_MESSAGE_DELIMITER, orderApprovalResponse.getFailureMessages()));
    }
}
