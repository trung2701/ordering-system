package com.coffee.ordering.system.application.outbox.scheduler.approval;

import com.coffee.ordering.system.application.communication.OrderApprovalRequestEventPublisher;
import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxScheduler;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.OrderApprovalOutboxRepository;
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
public class OrderApprovalOutboxScheduler implements OutboxScheduler {

    private final OutboxMapper outboxMapper;

    private final OrderApprovalOutboxHelper orderApprovalOutboxHelper;
    private final OrderApprovalOutboxRepository orderApprovalOutboxRepository;
    private final OrderApprovalRequestEventPublisher orderApprovalRequestEventPublisher;


    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}", initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void process() {
        orderApprovalOutboxHelper.getApprovalOutboxMessage(OutboxStatus.STARTED, SagaStatus.PROCESSING)
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(outboxMessages -> {
                    log.info("Received {} OrderApprovalOutboxMessage with ids: {}, sending to message bus!",
                            outboxMessages.size(),
                            outboxMessages.stream().map(outboxMessage ->
                                    outboxMessage.getId().toString()).collect(Collectors.joining(",")));

                    outboxMessages.forEach(outboxMessage ->
                            orderApprovalRequestEventPublisher.publish(outboxMessage, this::updateOutboxStatus));
                    log.info("{} OrderApprovalOutboxMessage sent to message bus!", outboxMessages.size());
                });
    }

    private void updateOutboxStatus(OrderApprovalOutboxMessage orderApprovalOutboxMessage, OutboxStatus outboxStatus) {
        orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
        orderApprovalOutboxRepository.save(outboxMapper.buildOrderApprovalOutboxEntity(orderApprovalOutboxMessage));
        log.info("OrderApprovalOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }
}
