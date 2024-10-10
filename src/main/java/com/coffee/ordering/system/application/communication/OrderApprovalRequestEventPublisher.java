package com.coffee.ordering.system.application.communication;

import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface OrderApprovalRequestEventPublisher {

    void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
                 BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback);
}
