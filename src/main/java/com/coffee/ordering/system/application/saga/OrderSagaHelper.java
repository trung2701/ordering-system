package com.coffee.ordering.system.application.saga;

import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.connectors.saga.SagaStatus;
import com.coffee.ordering.system.dataaccess.OrderRepository;
import com.coffee.ordering.system.dataaccess.entity.OrderEntity;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.exception.OrderNotFoundException;
import com.coffee.ordering.system.mappers.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaHelper {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    OrderDTO findOrder(String orderId) {
        return orderRepository.findById(UUID.fromString(orderId))
                .map(orderMapper::buildOrderDTO)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " could not be found!"));
    }

    void saveOrder(OrderDTO orderDTO) {
        OrderEntity orderEntity = orderMapper.buildOrderEntity(orderDTO);
        orderEntity.getItems().forEach(orderItem -> orderItem.setOrder(orderEntity));
        orderEntity.getAddress().setOrder(orderEntity);
        orderRepository.save(orderEntity);
    }

    public SagaStatus orderStatusToSagaStatus(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PAID -> SagaStatus.PROCESSING;
            case APPROVED -> SagaStatus.SUCCEEDED;
            case CANCELLING -> SagaStatus.COMPENSATING;
            case CANCELLED -> SagaStatus.COMPENSATED;
            default -> SagaStatus.STARTED;
        };
    }
}
