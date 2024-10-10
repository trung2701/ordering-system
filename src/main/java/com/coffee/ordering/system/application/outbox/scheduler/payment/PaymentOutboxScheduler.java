package com.coffee.ordering.system.application.outbox.scheduler.payment;

import com.coffee.ordering.system.application.communication.PaymentRequestEventPublisher;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxScheduler;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.PaymentOutboxRepository;
import com.coffee.ordering.system.mappers.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxScheduler implements OutboxScheduler {

    private final PaymentOutboxHelper paymentOutboxHelper;
    private final PaymentRequestEventPublisher paymentRequestEventPublisher;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final OutboxMapper outboxMapper;

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}", initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void process() {
        paymentOutboxHelper.getPaymentOutboxMessage(OutboxStatus.STARTED, SagaStatus.STARTED, SagaStatus.COMPENSATING)
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(outboxMessages -> {
                    log.info("Received {} OrderPaymentOutboxMessage with ids: {}, sending to message bus!",
                            outboxMessages.size(),
                            outboxMessages.stream().map(outboxMessage ->
                                    outboxMessage.getId().toString()).collect(Collectors.joining(",")));
                    outboxMessages.forEach(outboxMessage ->
                            paymentRequestEventPublisher.publish(outboxMessage, this::updateOutboxStatus));
                    log.info("{} OrderPaymentOutboxMessage sent to message bus!", outboxMessages.size());
                });

    }

    private void updateOutboxStatus(OrderPaymentOutboxMessage orderPaymentOutboxMessage, OutboxStatus outboxStatus) {
        orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
        paymentOutboxRepository.save(outboxMapper.buildPaymentOutboxEntity(orderPaymentOutboxMessage));
        log.info("OrderPaymentOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }
}
