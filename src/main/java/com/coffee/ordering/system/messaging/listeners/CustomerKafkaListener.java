package com.coffee.ordering.system.messaging.listeners;

import com.coffee.ordering.system.connectors.kafka.consumer.KafkaConsumer;
import com.coffee.ordering.system.connectors.kafka.model.CustomerEventModel;
import com.coffee.ordering.system.dataaccess.CustomerRepository;
import com.coffee.ordering.system.dataaccess.entity.CustomerEntity;
import com.coffee.ordering.system.exception.OrderException;
import com.coffee.ordering.system.mappers.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerKafkaListener implements KafkaConsumer<CustomerEventModel> {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.customer-group-id}", topics = "${order-service.customer-topic-name}")
    public void consume(@Payload CustomerEventModel customerModel,
                        @Header(KafkaHeaders.RECEIVED_KEY) String keys) {
        log.info("Customer {} create message received with keys {}", customerModel, keys);

        try {
            CustomerEntity customerEntity = customerMapper.buildCustomerEntity(customerModel);
            CustomerEntity savedCustomer = customerRepository.save(customerEntity);
            log.info("Customer created successfully with id: {}", savedCustomer.getId());
        } catch (DataAccessException e) {
            log.error("Failed to create customer in order database with id: {}. Error: {}", customerModel.getCustomerId(), e.getMessage());
            throw new OrderException("Failed to create customer id " + customerModel.getCustomerId(), e);
        }
    }
}
