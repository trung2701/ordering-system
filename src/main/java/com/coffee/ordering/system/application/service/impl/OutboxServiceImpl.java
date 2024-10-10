package com.coffee.ordering.system.application.service.impl;

import com.coffee.ordering.system.application.event.OrderCreatedOutboxEvent;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentEventPayload;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.application.saga.OrderSagaHelper;
import com.coffee.ordering.system.application.service.OutboxService;
import com.coffee.ordering.system.application.service.helper.OutboxHelper;
import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.entity.PaymentOrderOutboxEntity;
import com.coffee.ordering.system.mappers.OrderEventMapper;
import com.coffee.ordering.system.mappers.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.coffee.ordering.system.connectors.saga.SagaConstants.ORDER_SAGA_NAME;
import static com.coffee.ordering.system.utils.CoffeeUtils.stringify;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxMapper outboxMapper;
    private final OrderSagaHelper orderSagaHelper;
    private final OutboxHelper outboxHelper;
    private final OrderEventMapper orderEventMapper;

    public void createPaymentOrderOutbox(OrderCreatedOutboxEvent orderCreatedOutboxEvent) {
        OrderPaymentEventPayload eventPayload = orderEventMapper.buildOrderCreatedPaymentEventPayload(orderCreatedOutboxEvent);
        createPaymentOrderOutbox(eventPayload,
                orderCreatedOutboxEvent.getOrder().getOrderStatus(),
                orderSagaHelper.orderStatusToSagaStatus(orderCreatedOutboxEvent.getOrder().getOrderStatus()),
                OutboxStatus.STARTED,
                UUID.randomUUID());
    }

    public void createPaymentOrderOutbox(OrderPaymentEventPayload eventPayload, OrderStatus orderStatus,
                                         SagaStatus sagaStatus, OutboxStatus outboxStatus, UUID sagaId) {
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = OrderPaymentOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .createdAt(eventPayload.getCreatedAt())
                .type(ORDER_SAGA_NAME)
                .payload(stringify(eventPayload))
                .orderStatus(orderStatus)
                .sagaStatus(sagaStatus)
                .outboxStatus(outboxStatus)
                .build();
        PaymentOrderOutboxEntity entity = outboxMapper.buildPaymentOutboxEntity(orderPaymentOutboxMessage);
        outboxHelper.save(entity);
    }
}
