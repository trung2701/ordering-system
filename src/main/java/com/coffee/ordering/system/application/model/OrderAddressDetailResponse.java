package com.coffee.ordering.system.application.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class OrderAddressDetailResponse {
    @Id
    private UUID id;
    private String street;
    private String postalCode;
    private String city;
}
