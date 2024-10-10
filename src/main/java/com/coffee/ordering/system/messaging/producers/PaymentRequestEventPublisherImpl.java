package com.coffee.ordering.system.messaging.producers;

import com.coffee.ordering.system.application.communication.PaymentRequestEventPublisher;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentEventPayload;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.configuration.OrderEventProperties;
import com.coffee.ordering.system.connectors.kafka.model.PaymentRequestEventModel;
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
public class PaymentRequestEventPublisherImpl implements PaymentRequestEventPublisher {

    private final OrderEventMapper orderEventMapper;
    @Lazy
    private final KafkaProducer<String, PaymentRequestEventModel> kafkaProducer;
    private final OrderEventProperties orderEventProperties;


    @Override
    public void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
                        BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {
        OrderPaymentEventPayload orderPaymentEventPayload = objectify(orderPaymentOutboxMessage.getPayload(), OrderPaymentEventPayload.class);

        String sagaId = orderPaymentOutboxMessage.getSagaId().toString();

        log.info("Received OrderPaymentOutboxMessage for order id: {} and saga id: {}",
                orderPaymentEventPayload.getOrderId(),
                sagaId);

        try {
            PaymentRequestEventModel paymentEventModel = orderEventMapper
                    .buildPaymentRequestEventModel(sagaId, orderPaymentEventPayload);

            kafkaProducer.publish(orderEventProperties.getPaymentRequestTopicName(),
                    sagaId,
                    paymentEventModel,
                    kafkaProducer.callback(orderEventProperties.getPaymentRequestTopicName(),
                            orderPaymentOutboxMessage,
                            outboxCallback,
                            orderPaymentEventPayload.getOrderId()));

            log.info("OrderPaymentEventPayload sent to Kafka for order id: {} and saga id: {}",
                    orderPaymentEventPayload.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderPaymentEventPayload" +
                            " to kafka with order id: {} and saga id: {}, error: {}",
                    orderPaymentEventPayload.getOrderId(), sagaId, e.getMessage());
        }
    }
}
