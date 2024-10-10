package com.coffee.ordering.system.mappers;

import com.coffee.ordering.system.application.model.*;
import com.coffee.ordering.system.dataaccess.entity.CustomerEntity;
import com.coffee.ordering.system.dataaccess.entity.OrderAddressEntity;
import com.coffee.ordering.system.dataaccess.entity.OrderEntity;
import com.coffee.ordering.system.dataaccess.entity.OrderItemEntity;
import com.coffee.ordering.system.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;

import static com.coffee.ordering.system.common.CoffeeConstants.FAILURE_MESSAGE_DELIMITER;
import static java.util.Optional.ofNullable;

@Mapper(componentModel = "spring")
public abstract class OrderMapper {
    public abstract CustomerDTO buidCustomerDTO(CustomerEntity customerEntity);

    @Mapping(target = "orderId", ignore = true)
    @Mapping(source = "createOrderRequest.customerId", target = "customerId")
    @Mapping(source = "createOrderRequest.coffeeShopId", target = "coffeeShopId")
    @Mapping(source = "createOrderRequest.price", target = "price")
    @Mapping(source = "createOrderRequest.address", target = "deliveryAddress")
    @Mapping(source = "createOrderRequest.items", target = "items")
    @Mapping(target = "trackingId", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "failureMessages", ignore = true)
    public abstract OrderDTO buildOrderDTO(CreateOrderRequest createOrderRequest);

    @Mapping(source = "address.street", target = "street")
    @Mapping(source = "address.postalCode", target = "postalCode")
    @Mapping(source = "address.city", target = "city")
    protected abstract StreetAddress mapAddressToStreetAddress(OrderAddress address);

    protected abstract List<OrderItemDTO> mapOrderItemsToDTO(List<OrderItem> items);

    @Mapping(source = "orderItem.productId", target = "product.productId")
    @Mapping(source = "orderItem.quantity", target = "quantity")
    @Mapping(source = "orderItem.price", target = "price")
    @Mapping(source = "orderItem.subTotal", target = "subTotal")
    protected abstract OrderItemDTO mapOrderItemToDTO(OrderItem orderItem);

    @Mapping(target = "orderId", source = "id")
    @Mapping(source = "address", target = "deliveryAddress")
    @Mapping(source = "failureMessages", target = "failureMessages", qualifiedByName = "stringToList")
    public abstract OrderDTO buildOrderDTO(OrderEntity orderEntity);

    @Mapping(target = "orderId", source = "id")
    @Mapping(source = "address", target = "deliveryAddress")
    @Mapping(source = "failureMessages", target = "failureMessages", qualifiedByName = "stringToList")
    public abstract OrderDetailDTO buildOrderDetailDTO(OrderEntity orderEntity);

    @Mapping(source = "orderId", target = "id")
    @Mapping(source = "deliveryAddress", target = "address")
    @Mapping(source = "failureMessages", target = "failureMessages", qualifiedByName = "listToString")
    public abstract OrderEntity buildOrderEntity(OrderDTO orderDTO);

    protected abstract List<OrderItemDTO> mapOrderItemEntitiesToDTOs(List<OrderItemEntity> items);

    @Mapping(target = "product.productId", source = "productId")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderItemId", source = "id")
    protected abstract OrderItemDTO mapOrderItemEntityToDTO(OrderItemEntity item);

    protected abstract List<OrderItemEntity> mapOrderItemDTOsToEntities(List<OrderItemDTO> items);

    @Mapping(target = "id", source = "orderItemId")
    @Mapping(target = "productId", source = "orderItemDTO.product.productId")
    protected abstract OrderItemEntity orderItemDTOToOrderItemEntity(OrderItemDTO orderItemDTO);

    protected abstract StreetAddress map(OrderAddressEntity addressEntity);

    protected abstract OrderAddressEntity map(StreetAddress streetAddress);

    @Named("stringToList")
    protected List<String> stringToList(String failureMessages) {
        return ofNullable(failureMessages)
                .filter(StringUtils::isNotBlank)
                .map(s -> s.split(FAILURE_MESSAGE_DELIMITER))
                .map(Arrays::asList)
                .orElse(null);
    }

    @Named("listToString")
    protected String listToString(List<String> failureMessages) {
        return ofNullable(failureMessages).map(s -> String.join(FAILURE_MESSAGE_DELIMITER, s)).orElse(null);
    }

    @Mapping(target = "orderTrackingId", source = "order.trackingId")
    public abstract CreateOrderResponse buildCreateOrderResponse(OrderDTO order, String message);

    @Mapping(source = "trackingId", target = "orderTrackingId")
    @Mapping(source = "orderStatus", target = "orderStatus")
    @Mapping(source = "failureMessages", target = "failureMessages")
    public abstract TrackOrderResponse buildTrackOrderResponse(OrderDTO order);

    @Mapping(source = "detailDTO", target = "detail")
    public abstract OrderDetailResponse buildOrderDetailResponse(OrderDetailDTO detailDTO);
}
