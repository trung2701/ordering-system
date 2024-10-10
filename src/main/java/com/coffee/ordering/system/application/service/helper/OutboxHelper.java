package com.coffee.ordering.system.application.service.helper;

import com.coffee.ordering.system.dataaccess.PaymentOutboxRepository;
import com.coffee.ordering.system.dataaccess.entity.PaymentOrderOutboxEntity;
import com.coffee.ordering.system.exception.OrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxHelper {
    private final PaymentOutboxRepository paymentOutboxRepository;

    @Transactional
    public void save(PaymentOrderOutboxEntity entity) {
        try {
            paymentOutboxRepository.save(entity);
            log.info("OrderPaymentOutboxMessage saved successfully with outbox id: {}", entity.getId());
        } catch (Exception ex) {
            log.error("Failed to save OrderPaymentOutboxMessage with outbox id: {}. Error: {}",
                    entity.getId(), ex.getMessage(), ex);
            throw new OrderException("Could not save OrderPaymentOutboxMessage with outbox id: " +
                    entity.getId(), ex);
        }

    }
}
