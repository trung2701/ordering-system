package com.coffee.ordering.system.messaging.listeners;

import com.coffee.ordering.system.application.communication.PaymentResponseEventListener;
import com.coffee.ordering.system.application.model.PaymentResponse;
import com.coffee.ordering.system.connectors.kafka.consumer.KafkaConsumer;
import com.coffee.ordering.system.connectors.kafka.model.PaymentResponseEventModel;
import com.coffee.ordering.system.exception.OrderNotFoundException;
import com.coffee.ordering.system.mappers.OrderEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseEventModel> {

    private final OrderEventMapper orderEventMapper;

    private final PaymentResponseEventListener paymentResponseEventListener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
            topics = "${order-service.payment-response-topic-name}", containerFactory = "jsonKafkaListenerContainerFactory")
    public void consume(@Payload PaymentResponseEventModel eventModel, @Header(KafkaHeaders.RECEIVED_KEY) String keys) {
        log.info("{} number of payment responses received with keys:{}", eventModel, keys);

        try {
            PaymentResponse paymentResponse = orderEventMapper.buildPaymentResponse(eventModel);
            switch (eventModel.getPaymentStatus()) {
                case COMPLETED -> {
                    log.info("Processing successful payment for order id: {}", eventModel.getOrderId());
                    paymentResponseEventListener.paymentCompleted(paymentResponse);
                }
                case FAILED, CANCELLED -> {
                    log.info("Processing unsuccessful payment for order id: {}", eventModel.getOrderId());
                    paymentResponseEventListener.paymentCancelled(paymentResponse);
                }
            }
        } catch (OptimisticLockingFailureException e) {
            log.error("Optimistic locking exp in PaymentResponseKafkaListener for order id: {}", eventModel.getOrderId());
        } catch (OrderNotFoundException e) {
            log.error("No order found for order id: {}", eventModel.getOrderId());
        }
    }
}
