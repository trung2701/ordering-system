package com.coffee.ordering.system.application.outbox.scheduler.payment;

import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.PaymentOutboxRepository;
import com.coffee.ordering.system.dataaccess.entity.PaymentOrderOutboxEntity;
import com.coffee.ordering.system.exception.OrderException;
import com.coffee.ordering.system.mappers.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.coffee.ordering.system.connectors.saga.SagaConstants.ORDER_SAGA_NAME;
import static java.util.Arrays.asList;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxHelper {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final OutboxMapper outboxMapper;

    @Transactional(readOnly = true)
    public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessage(OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        return Optional.of(paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatusIn(ORDER_SAGA_NAME,
                        outboxStatus,
                        asList(sagaStatus))
                .orElseThrow(NoSuchElementException::new)
                .stream()
                .map(outboxMapper::buildOrderPaymentOutboxMessage)
                .toList());
    }

    @Transactional(readOnly = true)
    public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessage(UUID sagaId, SagaStatus... sagaStatus) {
        return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatusIn(ORDER_SAGA_NAME, sagaId, asList(sagaStatus))
                .map(outboxMapper::buildOrderPaymentOutboxMessage);
    }

    @Transactional
    public void deletePaymentOutboxMessage(OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(ORDER_SAGA_NAME, outboxStatus, asList(sagaStatus));
    }

    @Transactional
    public void save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
        PaymentOrderOutboxEntity response = paymentOutboxRepository.save(outboxMapper.buildPaymentOutboxEntity(orderPaymentOutboxMessage));
        if (response == null) {
            log.error("Could not save OrderPaymentOutboxMessage with outbox id: {}", orderPaymentOutboxMessage.getId());
            throw new OrderException("Could not save OrderPaymentOutboxMessage with outbox id: " +
                    orderPaymentOutboxMessage.getId());
        }
        log.info("OrderPaymentOutboxMessage saved with outbox id: {}", orderPaymentOutboxMessage.getId());
    }

}
