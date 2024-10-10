package com.coffee.ordering.system.application.communication;

import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface PaymentRequestEventPublisher {

    void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
                 BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback);
}
