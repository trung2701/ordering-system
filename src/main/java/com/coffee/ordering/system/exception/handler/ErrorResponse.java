package com.coffee.ordering.system.exception.handler;

import lombok.Builder;

@Builder
public record ErrorResponse(String code, String message) {
}
