package com.coffee.ordering.system.application.model;

import com.coffee.ordering.system.common.OrderApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderApprovalResponse {
    private String id;
    private String sagaId;
    private String orderId;
    private String coffeeShopId;
    private LocalDateTime createdAt;
    private OrderApprovalStatus orderApprovalStatus;
    private List<String> failureMessages;
}
