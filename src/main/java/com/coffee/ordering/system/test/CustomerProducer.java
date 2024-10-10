package com.coffee.ordering.system.test;

import com.coffee.ordering.system.configuration.OrderEventProperties;
import com.coffee.ordering.system.connectors.kafka.model.CustomerEventModel;
import com.coffee.ordering.system.connectors.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerProducer {
    private final KafkaProducer<String, CustomerEventModel> kafkaProducer;
    private final OrderEventProperties orderEventProperties;

    @Value("${order-service.customer-topic-name}")
    private String customerTopic;

    void produce() {
        CustomerEventModel e = new CustomerEventModel();
        e.setCustomerId("d215b5f8-0249-4dc5-89a3-51fd148cfb41");
        e.setFirstName("First");
        e.setLastName("User");
        e.setUsername("user_1");
        kafkaProducer.publish(customerTopic,
                "customer",
                e,
                (sendResult, throwable) -> {
                    if (throwable != null) {
                        log.error("Error sending message to Kafka: {}", throwable.getMessage());
                    } else {
                        log.info("Message sent successfully: {}", sendResult.getRecordMetadata());
                    }
                });
    }
}
