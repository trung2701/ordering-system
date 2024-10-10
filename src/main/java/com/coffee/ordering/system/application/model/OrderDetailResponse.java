package com.coffee.ordering.system.application.model;

import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.dataaccess.entity.OrderItemEntity;
import com.coffee.ordering.system.dto.CustomerDTO;
import com.coffee.ordering.system.dto.OrderDTO;
import com.coffee.ordering.system.dto.OrderDetailDTO;
import com.coffee.ordering.system.dto.StreetAddress;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse {
    private OrderDetailDTO detail;
}
