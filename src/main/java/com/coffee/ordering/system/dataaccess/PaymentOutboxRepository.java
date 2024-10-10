package com.coffee.ordering.system.dataaccess;

import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.entity.PaymentOrderOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentOutboxRepository extends JpaRepository<PaymentOrderOutboxEntity, UUID> {
    Optional<List<PaymentOrderOutboxEntity>> findByTypeAndOutboxStatusAndSagaStatusIn(String type,
                                                                                      OutboxStatus outboxStatus,
                                                                                      List<SagaStatus> sagaStatus);

    Optional<PaymentOrderOutboxEntity> findByTypeAndSagaIdAndSagaStatusIn(String type,
                                                                          UUID sagaId,
                                                                          List<SagaStatus> sagaStatus);

    void deleteByTypeAndOutboxStatusAndSagaStatusIn(String type,
                                                    OutboxStatus outboxStatus,
                                                    List<SagaStatus> sagaStatus);
}