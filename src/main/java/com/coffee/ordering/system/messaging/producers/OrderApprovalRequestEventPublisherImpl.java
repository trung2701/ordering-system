package com.coffee.ordering.system.messaging.producers;

import com.coffee.ordering.system.application.communication.OrderApprovalRequestEventPublisher;
import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalEventPayload;
import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.configuration.OrderEventProperties;
import com.coffee.ordering.system.connectors.kafka.model.OrderApprovalRequestEventModel;
import com.coffee.ordering.system.connectors.kafka.producer.KafkaProducer;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.mappers.OrderEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static com.coffee.ordering.system.utils.CoffeeUtils.objectify;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalRequestEventPublisherImpl implements OrderApprovalRequestEventPublisher {
    private final OrderEventProperties orderEventProperties;
    @Lazy
    private final KafkaProducer<String, OrderApprovalRequestEventModel> kafkaProducer;
    private final OrderEventMapper orderEventMapper;

    @Override
    public void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage, BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback) {
        OrderApprovalEventPayload eventPayload = objectify(orderApprovalOutboxMessage.getPayload(), OrderApprovalEventPayload.class);

        String sagaId = orderApprovalOutboxMessage.getSagaId().toString();

        log.info("Received OrderApprovalOutboxMessage for order id: {} and saga id: {}", eventPayload.getOrderId(), sagaId);

        try {
            OrderApprovalRequestEventModel eventModel = orderEventMapper.buildOrderApprovalRequestEventModel(sagaId, eventPayload);

            kafkaProducer.publish(orderEventProperties.getOrderApprovalRequestTopicName(),
                    sagaId,
                    eventModel,
                    kafkaProducer.callback(orderEventProperties.getOrderApprovalRequestTopicName(),
                            orderApprovalOutboxMessage,
                            outboxCallback,
                            eventPayload.getOrderId()));

            log.info("OrderApprovalEventPayload sent to kafka for order id: {} and saga id: {}",
                    eventModel.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderApprovalEventPayload to kafka for order id: {} and saga id: {}," +
                    " error: {}", eventPayload.getOrderId(), sagaId, e.getMessage());
        }
    }
}
