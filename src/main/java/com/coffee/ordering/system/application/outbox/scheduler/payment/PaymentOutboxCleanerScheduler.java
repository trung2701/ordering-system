package com.coffee.ordering.system.application.outbox.scheduler.payment;

import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxScheduler;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {

    private final PaymentOutboxHelper paymentOutboxHelper;

    public PaymentOutboxCleanerScheduler(PaymentOutboxHelper paymentOutboxHelper) {
        this.paymentOutboxHelper = paymentOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void process() {
        paymentOutboxHelper.getPaymentOutboxMessage(OutboxStatus.COMPLETED, SagaStatus.SUCCEEDED, SagaStatus.FAILED, SagaStatus.COMPENSATED)
                .ifPresent(outboxMessages -> {
                    log.info("Received {} OrderPaymentOutboxMessage for clean-up. The payloads: {}",
                            outboxMessages.size(),
                            outboxMessages.stream()
                                    .map(OrderPaymentOutboxMessage::getPayload)
                                    .collect(Collectors.joining("\n")));

                    paymentOutboxHelper.deletePaymentOutboxMessage(OutboxStatus.COMPLETED, SagaStatus.SUCCEEDED,
                            SagaStatus.FAILED, SagaStatus.COMPENSATED);
                    log.info("{} OrderPaymentOutboxMessage deleted!", outboxMessages.size());
                });
    }
}
