package com.coffee.ordering.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class OrderItemDTO {
    private final ProductDTO product;
    private final int quantity;
    private final BigDecimal price;
    private final BigDecimal subTotal;
    private UUID orderId;
    private UUID orderItemId;
}
