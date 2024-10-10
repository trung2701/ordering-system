package com.coffee.ordering.system.application.outbox.scheduler.approval;

import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.OrderApprovalOutboxRepository;
import com.coffee.ordering.system.dataaccess.entity.OrderApprovalOutboxEntity;
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
public class OrderApprovalOutboxHelper {

    private final OutboxMapper outboxMapper;
    private final OrderApprovalOutboxRepository orderApprovalOutboxRepository;

    @Transactional(readOnly = true)
    public Optional<List<OrderApprovalOutboxMessage>> getApprovalOutboxMessage(OutboxStatus outboxStatus,
                                                                               SagaStatus... sagaStatus) {
        return Optional.of(orderApprovalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatusIn(ORDER_SAGA_NAME,
                        outboxStatus,
                        asList(sagaStatus))
                .orElseThrow(NoSuchElementException::new)
                .stream()
                .map(outboxMapper::buildOrderApprovalOutboxMessage)
                .toList());
    }

    @Transactional(readOnly = true)
    public Optional<OrderApprovalOutboxMessage> getApprovalOutboxMessage(UUID sagaId, SagaStatus... sagaStatus) {
        return orderApprovalOutboxRepository.findByTypeAndSagaIdAndSagaStatusIn(ORDER_SAGA_NAME, sagaId, asList(sagaStatus))
                .map(outboxMapper::buildOrderApprovalOutboxMessage);
    }

    @Transactional
    public void save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
        try {
            OrderApprovalOutboxEntity orderApprovalOutboxEntity = outboxMapper.buildOrderApprovalOutboxEntity(orderApprovalOutboxMessage);
            OrderApprovalOutboxEntity response = orderApprovalOutboxRepository.save(orderApprovalOutboxEntity);
            log.info("OrderApprovalOutboxMessage saved with outbox id: {}", response.getId());
        } catch (Exception e) {
            log.error("Fail to save OrderApprovalOutboxMessage with outbox id: {}", orderApprovalOutboxMessage.getId());
            throw new OrderException("Fail to save OrderApprovalOutboxMessage with outbox id: " + orderApprovalOutboxMessage.getId());
        }
    }

    @Transactional
    public void deleteApprovalOutboxMessage(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        orderApprovalOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, asList(sagaStatus));
    }
}
