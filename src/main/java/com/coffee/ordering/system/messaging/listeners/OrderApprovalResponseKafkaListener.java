package com.coffee.ordering.system.messaging.listeners;

import com.coffee.ordering.system.application.communication.OrderApprovalResponseEventListener;
import com.coffee.ordering.system.application.model.OrderApprovalResponse;
import com.coffee.ordering.system.connectors.kafka.consumer.KafkaConsumer;
import com.coffee.ordering.system.connectors.kafka.model.OrderApprovalResponseEventModel;
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

import static com.coffee.ordering.system.common.CoffeeConstants.FAILURE_MESSAGE_DELIMITER;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalResponseKafkaListener implements KafkaConsumer<OrderApprovalResponseEventModel> {

    private final OrderEventMapper orderEventMapper;

    private final OrderApprovalResponseEventListener orderApprovalResponseEventListener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.order-approval-consumer-group-id}", topics = "${order-service.order-approval-response-topic-name}")
    public void consume(@Payload OrderApprovalResponseEventModel eventModel, @Header(KafkaHeaders.RECEIVED_KEY) String keys) {
        log.info("Order approval responses {} received with keys {}", eventModel, keys);
        try {
            OrderApprovalResponse orderApprovalResponse = orderEventMapper.buildOrderApprovalResponse(eventModel);
            switch (eventModel.getOrderApprovalStatus()) {
                case APPROVED -> {
                    log.info("Processing approved order for order id: {}", eventModel.getOrderId());
                    orderApprovalResponseEventListener.orderApproved(orderApprovalResponse);
                }
                case REJECTED -> {
                    log.info("Processing rejected order for order id: {}, with failure messages: {}",
                            eventModel.getOrderId(), String.join(FAILURE_MESSAGE_DELIMITER, eventModel.getFailureMessages()));
                    orderApprovalResponseEventListener.orderRejected(orderApprovalResponse);
                }
            }
        } catch (OptimisticLockingFailureException e) {
            log.error("Optimistic locking exp in OrderApprovalResponseKafkaListener for order id: {}",
                    eventModel.getOrderId());
        } catch (OrderNotFoundException e) {
            log.error("No order found for order id: {}", eventModel.getOrderId());
        }

    }
}
