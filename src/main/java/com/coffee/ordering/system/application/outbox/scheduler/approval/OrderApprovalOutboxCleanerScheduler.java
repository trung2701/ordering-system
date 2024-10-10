package com.coffee.ordering.system.application.outbox.scheduler.approval;

import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxScheduler;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static com.coffee.ordering.system.connectors.saga.SagaConstants.ORDER_SAGA_NAME;

@Slf4j
@Component
public class OrderApprovalOutboxCleanerScheduler implements OutboxScheduler {

    private final OrderApprovalOutboxHelper orderApprovalOutboxHelper;

    public OrderApprovalOutboxCleanerScheduler(OrderApprovalOutboxHelper orderApprovalOutboxHelper) {
        this.orderApprovalOutboxHelper = orderApprovalOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void process() {
        orderApprovalOutboxHelper.getApprovalOutboxMessage(OutboxStatus.COMPLETED, SagaStatus.SUCCEEDED, SagaStatus.FAILED, SagaStatus.COMPENSATED)
                .ifPresent(outboxMessages -> {
                    log.info("Received {} OrderApprovalOutboxMessage for clean-up. The payloads: {}",
                            outboxMessages.size(),
                            outboxMessages.stream().map(OrderApprovalOutboxMessage::getPayload)
                                    .collect(Collectors.joining("\n")));

                    orderApprovalOutboxHelper.deleteApprovalOutboxMessage(ORDER_SAGA_NAME, OutboxStatus.COMPLETED,
                            SagaStatus.SUCCEEDED, SagaStatus.FAILED, SagaStatus.COMPENSATED);
                    log.info("{} OrderApprovalOutboxMessage deleted!", outboxMessages.size());
                });

    }
}
