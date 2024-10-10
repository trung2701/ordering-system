package com.coffee.ordering.system.application.saga;

import com.coffee.ordering.system.application.event.OrderPaidEvent;
import com.coffee.ordering.system.application.model.PaymentResponse;
import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalEventPayload;
import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.application.outbox.scheduler.approval.OrderApprovalOutboxHelper;
import com.coffee.ordering.system.application.outbox.scheduler.payment.PaymentOutboxHelper;
import com.coffee.ordering.system.application.service.OrderService;
import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.common.PaymentStatus;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.connectors.saga.SagaStep;
import com.coffee.ordering.system.dataaccess.OrderRepository;
import com.coffee.ordering.system.dataaccess.entity.OrderEntity;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.exception.OrderException;
import com.coffee.ordering.system.exception.OrderNotFoundException;
import com.coffee.ordering.system.mappers.OrderEventMapper;
import com.coffee.ordering.system.mappers.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static com.coffee.ordering.system.common.CoffeeConstants.UTC;
import static com.coffee.ordering.system.connectors.saga.SagaConstants.ORDER_SAGA_NAME;
import static com.coffee.ordering.system.utils.CoffeeUtils.stringify;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final OrderApprovalOutboxHelper approvalOutboxHelper;
    private final OrderSagaHelper orderSagaHelper;
    private final OrderMapper orderMapper;
    private final OrderEventMapper orderEventMapper;

    @Override
    @Transactional
    public void process(PaymentResponse paymentResponse) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessage(
                        UUID.fromString(paymentResponse.getSagaId()),
                        SagaStatus.STARTED);

        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed!", paymentResponse.getSagaId());
            return;
        }

        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

        OrderPaidEvent domainEvent = completePaymentForOrder(paymentResponse);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder().getOrderStatus());

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage,
                domainEvent.getOrder().getOrderStatus(), sagaStatus));



        OrderApprovalEventPayload payload = orderEventMapper.buildOrderApprovalEventPayload(domainEvent);
        OrderApprovalOutboxMessage outboxMessage = getOutboxMessage(paymentResponse, payload, domainEvent, sagaStatus);
        approvalOutboxHelper.save(outboxMessage);

        log.info("Order with id: {} is paid", domainEvent.getOrder().getOrderId());
    }

    private static OrderApprovalOutboxMessage getOutboxMessage(PaymentResponse paymentResponse, OrderApprovalEventPayload payload, OrderPaidEvent domainEvent, SagaStatus sagaStatus) {
        return OrderApprovalOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(UUID.fromString(paymentResponse.getSagaId()))
                .createdAt(payload.getCreatedAt())
                .type(ORDER_SAGA_NAME)
                .payload(stringify(payload))
                .orderStatus(domainEvent.getOrder().getOrderStatus())
                .sagaStatus(sagaStatus)
                .outboxStatus(OutboxStatus.STARTED)
                .build();
    }

    @Override
    @Transactional
    public void rollback(PaymentResponse paymentResponse) {

        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessage(
                        UUID.fromString(paymentResponse.getSagaId()),
                        getCurrentSagaStatus(paymentResponse.getPaymentStatus()));

        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed!", paymentResponse.getSagaId());
            return;
        }

        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

        OrderDTO order = rollbackPaymentForOrder(paymentResponse);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage,
                order.getOrderStatus(), sagaStatus));

        if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
            approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(paymentResponse.getSagaId(),
                    order.getOrderStatus(), sagaStatus));
        }

        log.info("Order with id: {} is cancelled", order.getOrderId());
    }

    private OrderEntity findOrder(String orderId) {
        return orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " could not be found!"));
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
                                                                     OrderStatus orderStatus,
                                                                     SagaStatus sagaStatus) {
        orderPaymentOutboxMessage.setProcessedAt(LocalDateTime.now(ZoneId.of(UTC)));
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        return orderPaymentOutboxMessage;
    }

    protected OrderPaidEvent completePaymentForOrder(PaymentResponse paymentResponse) {
        log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
        OrderEntity order = findOrder(paymentResponse.getOrderId());
        OrderDTO orderDTO = orderMapper.buildOrderDTO(order);
        OrderPaidEvent domainEvent = orderService.payOrder(orderDTO);
        order.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(order);
        return domainEvent;
    }

    private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case COMPLETED -> new SagaStatus[]{SagaStatus.STARTED};
            case CANCELLED -> new SagaStatus[]{SagaStatus.PROCESSING};
            case FAILED -> new SagaStatus[]{SagaStatus.STARTED, SagaStatus.PROCESSING};
            default -> new SagaStatus[]{};
        };
    }

    private OrderDTO rollbackPaymentForOrder(PaymentResponse paymentResponse) {
        log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
        OrderEntity order = findOrder(paymentResponse.getOrderId());
        OrderDTO orderDTO = orderMapper.buildOrderDTO(order);
        orderService.cancelOrder(orderDTO, paymentResponse.getFailureMessages());
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setFailureMessages(String.join(",", paymentResponse.getFailureMessages()));
        orderRepository.save(order);
        return orderDTO;
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(String sagaId,
                                                                       OrderStatus orderStatus,
                                                                       SagaStatus sagaStatus) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
                approvalOutboxHelper.getApprovalOutboxMessage(
                        UUID.fromString(sagaId),
                        SagaStatus.COMPENSATING);
        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            throw new OrderException("Approval outbox message could not be found in " +
                    SagaStatus.COMPENSATING.name() + " status!");
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
        orderApprovalOutboxMessage.setProcessedAt(LocalDateTime.now(ZoneId.of(UTC)));
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        return orderApprovalOutboxMessage;
    }
}
