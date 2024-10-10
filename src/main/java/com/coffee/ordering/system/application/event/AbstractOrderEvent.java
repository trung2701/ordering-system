package com.coffee.ordering.system.application.event;

import com.coffee.ordering.system.common.AppEvent;
import com.coffee.ordering.system.dto.OrderDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public abstract class AbstractOrderEvent implements AppEvent<OrderDTO> {
    private final OrderDTO order;
    private final LocalDateTime createdAt;
}
