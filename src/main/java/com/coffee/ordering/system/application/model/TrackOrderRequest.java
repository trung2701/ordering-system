package com.coffee.ordering.system.application.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TrackOrderRequest {
    @NotNull
    private final UUID orderTrackingId;
}
