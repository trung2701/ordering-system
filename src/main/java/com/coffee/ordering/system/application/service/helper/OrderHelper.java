package com.coffee.ordering.system.application.service.helper;

import com.coffee.ordering.system.application.model.OrderDetailResponse;
import com.coffee.ordering.system.application.model.TrackOrderRequest;
import com.coffee.ordering.system.application.model.TrackOrderResponse;
import com.coffee.ordering.system.dataaccess.OrderRepository;
import com.coffee.ordering.system.dataaccess.entity.OrderEntity;
import com.coffee.ordering.system.dataaccess.entity.OrderItemEntity;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.dto.OrderDetailDTO;
import com.coffee.ordering.system.dto.OrderItemDTO;
import com.coffee.ordering.system.exception.OrderException;
import com.coffee.ordering.system.exception.OrderNotFoundException;
import com.coffee.ordering.system.mappers.OrderMapper;
import com.coffee.ordering.system.shop.v1.api.CoffeeShopApi;
import com.coffee.ordering.system.shop.v1.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderHelper {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CoffeeShopApi coffeeShopApi;

    @Transactional
    public void saveOrder(OrderDTO orderDTO) {
        OrderEntity orderEntity = orderMapper.buildOrderEntity(orderDTO);
        orderEntity.getItems().forEach(orderItem -> orderItem.setOrder(orderEntity));
        orderEntity.getAddress().setOrder(orderEntity);
        try {
            OrderEntity savedOrder = orderRepository.save(orderEntity);
            log.info("Order is saved with id: {}", savedOrder.getId());
        } catch (Exception ex) {
            log.error("Could not save order: {}", ex.getMessage(), ex);
            throw new OrderException("Could not save order!", ex);  // Pass the original exception as a cause
        }
    }

    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(TrackOrderRequest trackOrderRequest) {
        OrderDTO orderDTO = orderRepository.findByTrackingId(trackOrderRequest.getOrderTrackingId())
                .map(orderMapper::buildOrderDTO)
                .orElseThrow(() -> {
                    log.warn("Could not find order with tracking id: {}", trackOrderRequest.getOrderTrackingId());
                    return new OrderNotFoundException("Could not find order with tracking id: " +
                            trackOrderRequest.getOrderTrackingId());
                });
        return orderMapper.buildTrackOrderResponse(orderDTO);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(UUID trackingId) {
        OrderEntity order = findOrderByTrackingId(trackingId);
        OrderDetailDTO orderDTO = orderMapper.buildOrderDetailDTO(order);
        updateOrderItemsWithProductDetails(orderDTO);
        return orderMapper.buildOrderDetailResponse(orderDTO);
    }

    private OrderEntity findOrderByTrackingId(UUID trackingId) {
        return orderRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> {
                    log.warn("Could not find order with tracking id: {}", trackingId);
                    return new OrderNotFoundException("Could not find order with tracking id: " + trackingId);
                });
    }

    private void updateOrderItemsWithProductDetails(OrderDetailDTO orderDTO) {
        List<Product> products = coffeeShopApi.getShopById(orderDTO.getCoffeeShopId()).getProducts();
        orderDTO.setItems(orderDTO.getItems().stream()
                .peek(itemDTO -> updateProductDetails(itemDTO, products))
                .toList());
    }

    private void updateProductDetails(OrderItemDTO itemDTO, List<Product> products) {
        products.stream()
                .filter(product -> product.getProductId().equals(itemDTO.getProduct().getProductId()))
                .findFirst()
                .ifPresent(product -> {
                    itemDTO.getProduct().setName(product.getName());
                    itemDTO.getProduct().setPrice(new BigDecimal(product.getPrice()));
                });
    }
}
