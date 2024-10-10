package com.coffee.ordering.system.mappers;

import com.coffee.ordering.system.application.event.OrderCancelledEvent;
import com.coffee.ordering.system.application.event.OrderCreatedOutboxEvent;
import com.coffee.ordering.system.application.event.OrderPaidEvent;
import com.coffee.ordering.system.application.model.OrderApprovalResponse;
import com.coffee.ordering.system.application.model.PaymentResponse;
import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalEventPayload;
import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalEventProduct;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentEventPayload;
import com.coffee.ordering.system.common.OrderApprovalStatus;
import com.coffee.ordering.system.connectors.kafka.model.*;
import com.coffee.ordering.system.dto.OrderItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Mapper(componentModel = "spring")
public abstract class OrderEventMapper {

    @Mapping(target = "customerId", source = "order.customerId", qualifiedByName = "uuidToString")
    @Mapping(target = "orderId", source = "order.orderId", qualifiedByName = "uuidToString")
    @Mapping(target = "price", source = "order.price")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "paymentOrderStatus", expression = "java(com.coffee.ordering.system.connectors.kafka.model.PaymentStatus.PENDING.name())")
    public abstract OrderPaymentEventPayload buildOrderCreatedPaymentEventPayload(OrderCreatedOutboxEvent orderCreatedOutboxEvent);

    @Mapping(target = "customerId", source = "order.customerId", qualifiedByName = "uuidToString")
    @Mapping(target = "orderId", source = "order.orderId", qualifiedByName = "uuidToString")
    @Mapping(target = "price", source = "order.price")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "paymentOrderStatus", expression = "java(com.coffee.ordering.system.connectors.kafka.model.PaymentStatus.CANCELLED.name())")
    public abstract OrderPaymentEventPayload buildOrderCancelledPaymentEventPayload(OrderCancelledEvent orderCancelledEvent);

    @Named("uuidToString")
    protected String uuidToString(UUID uuid) {
        return ofNullable(uuid).map(UUID::toString).orElse(null);
    }

    @Mapping(target = "orderId", expression = "java(orderPaidEvent.getOrder().getOrderId().toString())")
    @Mapping(target = "coffeeShopId", expression = "java(orderPaidEvent.getOrder().getCoffeeShopId().toString())")
    @Mapping(target = "shopOrderStatus", expression = "java(com.coffee.ordering.system.connectors.kafka.model.CoffeeShopOrderStatus.PAID.name())")
    @Mapping(target = "products", source = "orderPaidEvent.order.items")
    @Mapping(target = "price", source = "orderPaidEvent.order.price")
    @Mapping(target = "createdAt", source = "orderPaidEvent.createdAt")
    public abstract OrderApprovalEventPayload buildOrderApprovalEventPayload(OrderPaidEvent orderPaidEvent);

    protected List<OrderApprovalEventProduct> mapOrderItemsToProducts(List<OrderItemDTO> items) {
        return items.stream()
                .map(orderItem -> OrderApprovalEventProduct.builder()
                        .id(orderItem.getProduct().getProductId().toString())
                        .quantity(orderItem.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    @Mapping(target = "orderApprovalStatus", source = "responseEventModel", qualifiedByName = "mapOrderApprovalStatus")
    public abstract OrderApprovalResponse buildOrderApprovalResponse(OrderApprovalResponseEventModel responseEventModel);

    @Named("mapOrderApprovalStatus")
    protected OrderApprovalStatus mapOrderApprovalStatus(OrderApprovalResponseEventModel responseEventModel) {
        return OrderApprovalStatus.valueOf(responseEventModel.getOrderApprovalStatus().name());
    }

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "paymentId", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "customerId", source = "orderPaymentEventPayload.customerId")
    @Mapping(target = "orderId", source = "orderPaymentEventPayload.orderId")
    @Mapping(target = "price", source = "orderPaymentEventPayload.price")
    @Mapping(target = "createdAt", source = "orderPaymentEventPayload.createdAt")
    @Mapping(target = "paymentStatus", source = "orderPaymentEventPayload.paymentOrderStatus", qualifiedByName = "stringToPaymentStatus")
    public abstract PaymentRequestEventModel buildPaymentRequestEventModel(String sagaId, OrderPaymentEventPayload orderPaymentEventPayload);

    @Named("stringToPaymentStatus")
    protected PaymentStatus stringToPaymentStatus(String paymentOrderStatus) {
        return PaymentStatus.valueOf(paymentOrderStatus);
    }

    @Mapping(target = "id", source = "paymentResponseModel.id")
    @Mapping(target = "sagaId", source = "paymentResponseModel.sagaId")
    @Mapping(target = "paymentId", source = "paymentResponseModel.paymentId")
    @Mapping(target = "customerId", source = "paymentResponseModel.customerId")
    @Mapping(target = "orderId", source = "paymentResponseModel.orderId")
    @Mapping(target = "price", source = "paymentResponseModel.price")
    @Mapping(target = "createdAt", expression = "java(toLocalDateTime(paymentResponseModel.getCreatedAt()))")
    @Mapping(target = "paymentStatus", expression = "java(mapPaymentStatus(paymentResponseModel.getPaymentStatus()))")
    @Mapping(target = "failureMessages", source = "paymentResponseModel.failureMessages")
    public abstract PaymentResponse buildPaymentResponse(PaymentResponseEventModel paymentResponseModel);

    protected LocalDateTime toLocalDateTime(String createdAt) {
        return LocalDateTime.parse(createdAt);
    }

    protected com.coffee.ordering.system.common.PaymentStatus mapPaymentStatus(PaymentStatus paymentStatus) {
        return com.coffee.ordering.system.common.PaymentStatus.valueOf(paymentStatus.name());
    }

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "orderId", source = "orderApprovalEventPayload.orderId")
    @Mapping(target = "coffeeShopId", source = "orderApprovalEventPayload.coffeeShopId")
    @Mapping(target = "coffeeShopOrderStatus", source = "orderApprovalEventPayload.shopOrderStatus")
    @Mapping(target = "products", source = "orderApprovalEventPayload.products")
    @Mapping(target = "price", source = "orderApprovalEventPayload.price")
    @Mapping(target = "createdAt", source = "orderApprovalEventPayload.createdAt")
    public abstract OrderApprovalRequestEventModel buildOrderApprovalRequestEventModel(String sagaId, OrderApprovalEventPayload orderApprovalEventPayload);

    protected CoffeeShopOrderStatus mapShopOrderStatus(String shopOrderStatus) {
        return CoffeeShopOrderStatus.valueOf(shopOrderStatus);
    }

    protected abstract List<Product> mapProducts(List<OrderApprovalEventProduct> products);

    protected abstract Product toProduct(OrderApprovalEventProduct orderApprovalEventProduct);
}
