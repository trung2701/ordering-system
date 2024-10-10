package com.coffee.ordering.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StreetAddress {
    private UUID id;
    private String street;
    private String postalCode;
    private String city;
}
