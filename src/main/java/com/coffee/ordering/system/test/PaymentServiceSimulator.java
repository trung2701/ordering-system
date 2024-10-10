package com.coffee.ordering.system.test;

import com.coffee.ordering.system.configuration.OrderEventProperties;
import com.coffee.ordering.system.connectors.kafka.consumer.KafkaConsumer;
import com.coffee.ordering.system.connectors.kafka.model.PaymentRequestEventModel;
import com.coffee.ordering.system.connectors.kafka.model.PaymentResponseEventModel;
import com.coffee.ordering.system.connectors.kafka.model.PaymentStatus;
import com.coffee.ordering.system.connectors.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.coffee.ordering.system.utils.CoffeeUtils.objectify;
import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceSimulator implements KafkaConsumer<String> {
    private final SimulatorMapper simulatorMapper;

    private final OrderEventProperties orderEventProperties;
    @Lazy
    private final KafkaProducer<String, PaymentResponseEventModel> kafkaProducer;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-simulator-group-id}",
            topics = "${order-service.payment-request-topic-name}", containerFactory = "stringKafkaListenerContainerFactory")
    public void consume(String messages, String keys) {
        log.info("Payment Service received payment request {}", messages);
        PaymentRequestEventModel eventModel = objectify(messages, PaymentRequestEventModel.class);
        PaymentResponseEventModel responseEventModel = simulatorMapper.buildPaymentResponseEventModel(eventModel);
        responseEventModel.setPaymentStatus(PaymentStatus.COMPLETED);
        publish(responseEventModel, keys);
    }

    private void publish(PaymentResponseEventModel messages, String keys) {
        kafkaProducer.publish(orderEventProperties.getPaymentResponseTopicName(),
                keys,
                messages, (res, ex) -> {
                    if (isNull(ex)) {
                        log.info("Successfully published payment response");
                    } else {
                        log.error("Failed to publish payment response", ex);
                    }
                });
    }
}
