package com.coffee.ordering.system.application.outbox.scheduler.approval;

import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.connectors.outbox.OutboxStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.OrderApprovalOutboxRepository;
import com.coffee.ordering.system.dataaccess.entity.OrderApprovalOutboxEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.coffee.ordering.system.connectors.saga.SagaConstants.ORDER_SAGA_NAME;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
class OrderApprovalOutboxSchedulerTest {

    @SpyBean
    private OrderApprovalOutboxRepository orderApprovalOutboxRepository;

    @Autowired
    private OrderApprovalOutboxScheduler orderApprovalOutboxScheduler;

    private OrderApprovalOutboxMessage orderApprovalOutboxMessage;

    @BeforeEach
    void setUp() {
        orderApprovalOutboxMessage = OrderApprovalOutboxMessage.builder().build();
        orderApprovalOutboxMessage.setId(UUID.randomUUID());
        orderApprovalOutboxMessage.setSagaId(UUID.randomUUID());
        orderApprovalOutboxMessage.setOutboxStatus(OutboxStatus.STARTED);
        orderApprovalOutboxMessage.setSagaStatus(SagaStatus.PROCESSING);
    }

    @Test
    void process_success() {

        OrderApprovalOutboxEntity outbox = OrderApprovalOutboxEntity.builder()
                .id(UUID.randomUUID())
                .sagaId(UUID.randomUUID())
                .payload("""
                        {"price": 200.00, "orderId": "6e0efa64-fffb-4584-acf5-f55a97a5666f", "products": [{"id": "d215b5f8-0249-4dc5-89a3-51fd148cfb48", "quantity": 1}, {"id": "d215b5f8-0249-4dc5-89a3-51fd148cfb49", "quantity": 3}], "createdAt": [2024, 10, 10, 7, 31, 24, 982337500], "coffeeShopId": "d215b5f8-0249-4dc5-89a3-51fd148cfb45", "shopOrderStatus": "PAID"}
                        """)
                .outboxStatus(OutboxStatus.STARTED)
                .build();
        when(orderApprovalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatusIn(ORDER_SAGA_NAME,
                OutboxStatus.STARTED, List.of(SagaStatus.PROCESSING))).thenReturn(Optional.of(List.of(outbox)));

        // Run the process method
        orderApprovalOutboxScheduler.process();

        verify(orderApprovalOutboxRepository, times(0))
                .save(any(OrderApprovalOutboxEntity.class));
    }
}