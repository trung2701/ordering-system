package com.coffee.ordering.system.dto;

import com.coffee.ordering.system.common.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDTO {
    private UUID orderId;
    private UUID coffeeShopId;
    private StreetAddress deliveryAddress;
    private BigDecimal price;
    private List<OrderItemDTO> items;

    private UUID trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;
}
