package com.coffee.ordering.system.application.saga;

import com.coffee.ordering.system.application.event.OrderCancelledEvent;
import com.coffee.ordering.system.application.model.OrderApprovalResponse;
import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.application.outbox.scheduler.approval.OrderApprovalOutboxHelper;
import com.coffee.ordering.system.application.outbox.scheduler.payment.PaymentOutboxHelper;
import com.coffee.ordering.system.application.service.OrderService;
import com.coffee.ordering.system.application.service.OutboxService;
import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.connectors.saga.SagaStep;
import com.coffee.ordering.system.dataaccess.PaymentOutboxRepository;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.exception.OrderException;
import com.coffee.ordering.system.mappers.OrderEventMapper;
import com.coffee.ordering.system.mappers.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static com.coffee.ordering.system.common.CoffeeConstants.UTC;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalSaga implements SagaStep<OrderApprovalResponse> {

    private final OrderService orderService;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final OrderApprovalOutboxHelper approvalOutboxHelper;
    private final OutboxMapper outboxMapper;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final OutboxService outboxService;
    private final OrderEventMapper orderEventMapper;

    @Override
    @Transactional
    public void process(OrderApprovalResponse orderApprovalResponse) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
                approvalOutboxHelper.getApprovalOutboxMessage(UUID.fromString(orderApprovalResponse.getSagaId()), SagaStatus.PROCESSING);

        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed!", orderApprovalResponse.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();

        OrderDTO order = approveOrder(orderApprovalResponse);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

        approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage,
                order.getOrderStatus(), sagaStatus));
        OrderPaymentOutboxMessage paymentOutboxMessage = getUpdatedPaymentOutboxMessage(orderApprovalResponse.getSagaId(), order.getOrderStatus(), sagaStatus);
        paymentOutboxRepository.save(outboxMapper.buildPaymentOutboxEntity(paymentOutboxMessage));

        log.info("Order with id: {} is approved", order.getOrderId());
    }

    @Override
    @Transactional
    public void rollback(OrderApprovalResponse orderApprovalResponse) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
                approvalOutboxHelper.getApprovalOutboxMessage(
                        UUID.fromString(orderApprovalResponse.getSagaId()),
                        SagaStatus.PROCESSING);

        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed!",
                    orderApprovalResponse.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();

        OrderCancelledEvent cancelledEvent = rollbackOrder(orderApprovalResponse);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(cancelledEvent.getOrder().getOrderStatus());

        approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage,
                cancelledEvent.getOrder().getOrderStatus(), sagaStatus));

        outboxService.createPaymentOrderOutbox(orderEventMapper.buildOrderCancelledPaymentEventPayload(cancelledEvent),
                cancelledEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(orderApprovalResponse.getSagaId()));

        log.info("Order with id: {} is cancelling", cancelledEvent.getOrder().getOrderId());
    }

    private OrderDTO approveOrder(OrderApprovalResponse orderApprovalResponse) {
        log.info("Approving order with id: {}", orderApprovalResponse.getOrderId());
        OrderDTO order = orderSagaHelper.findOrder(orderApprovalResponse.getOrderId());
        orderService.approveOrder(order);
        orderSagaHelper.saveOrder(order);
        return order;
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
                                                                       OrderStatus orderStatus, SagaStatus sagaStatus) {
        orderApprovalOutboxMessage.setProcessedAt(LocalDateTime.now(ZoneId.of(UTC)));
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        return orderApprovalOutboxMessage;
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse = paymentOutboxHelper
                .getPaymentOutboxMessage(UUID.fromString(sagaId), SagaStatus.PROCESSING);
        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            throw new OrderException("Payment outbox message cannot be found in " + SagaStatus.PROCESSING.name() + " state");
        }
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();
        orderPaymentOutboxMessage.setProcessedAt(LocalDateTime.now(ZoneId.of(UTC)));
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        return orderPaymentOutboxMessage;
    }

    private OrderCancelledEvent rollbackOrder(OrderApprovalResponse orderApprovalResponse) {
        log.info("Cancelling order with id: {}", orderApprovalResponse.getOrderId());
        OrderDTO order = orderSagaHelper.findOrder(orderApprovalResponse.getOrderId());
        OrderCancelledEvent domainEvent = orderService.cancelOrderPayment(order, orderApprovalResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        return domainEvent;
    }
}
