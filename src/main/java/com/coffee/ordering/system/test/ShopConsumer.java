package com.coffee.ordering.system.test;

import com.coffee.ordering.system.configuration.OrderEventProperties;
import com.coffee.ordering.system.connectors.kafka.consumer.KafkaConsumer;
import com.coffee.ordering.system.connectors.kafka.model.OrderApprovalRequestEventModel;
import com.coffee.ordering.system.connectors.kafka.model.OrderApprovalResponseEventModel;
import com.coffee.ordering.system.connectors.kafka.model.OrderApprovalStatus;
import com.coffee.ordering.system.connectors.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.coffee.ordering.system.utils.CoffeeUtils.objectify;
import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopConsumer implements KafkaConsumer<String> {

    private final SimulatorMapper simulatorMapper;
    private final OrderEventProperties properties;
    private final KafkaProducer<String, OrderApprovalResponseEventModel> producer;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.order-approval-consumer-simulator-group-id}",
            topics = "${order-service.order-approval-request-topic-name}", containerFactory = "stringKafkaListenerContainerFactory")
    public void consume(String messages, String keys) {
        log.info("Coffee shop received paid order request: {}", messages);
        OrderApprovalRequestEventModel eventModel = objectify(messages, OrderApprovalRequestEventModel.class);
        OrderApprovalResponseEventModel responseEventModel = simulatorMapper.buildOrderApprovalResponseEventModel(eventModel);
        responseEventModel.setOrderApprovalStatus(OrderApprovalStatus.APPROVED);
        publish(responseEventModel, keys);
    }

    private void publish(OrderApprovalResponseEventModel model, String key) {
        producer.publish(properties.getOrderApprovalResponseTopicName(),
                key,
                model, (res, ex) -> {
                    if (isNull(ex)) {
                        log.info("Approved order event sent");
                    } else {
                        log.info("Approved order event fail to publish");
                    }
                });
    }
}
