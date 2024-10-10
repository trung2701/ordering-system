package com.coffee.ordering.system.dataaccess;

import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.entity.OrderApprovalOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderApprovalOutboxRepository extends JpaRepository<OrderApprovalOutboxEntity, UUID> {
    Optional<List<OrderApprovalOutboxEntity>> findByTypeAndOutboxStatusAndSagaStatusIn(String type,
                                                                                       OutboxStatus outboxStatus,
                                                                                       List<SagaStatus> sagaStatus);

    Optional<OrderApprovalOutboxEntity> findByTypeAndSagaIdAndSagaStatusIn(String type,
                                                                           UUID sagaId,
                                                                           List<SagaStatus> sagaStatus);

    void deleteByTypeAndOutboxStatusAndSagaStatusIn(String type,
                                                    OutboxStatus outboxStatus,
                                                    List<SagaStatus> sagaStatus);
}
