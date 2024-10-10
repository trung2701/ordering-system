package com.coffee.ordering.system.application.service;

import com.coffee.ordering.system.application.event.OrderCreatedOutboxEvent;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentEventPayload;
import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;

import java.util.UUID;

public interface OutboxService {
    void createPaymentOrderOutbox(OrderCreatedOutboxEvent orderCreatedOutboxEvent);

    void createPaymentOrderOutbox(OrderPaymentEventPayload paymentEventPayload, OrderStatus orderStatus,
                                  SagaStatus sagaStatus, OutboxStatus outboxStatus, UUID sagaId);
}
