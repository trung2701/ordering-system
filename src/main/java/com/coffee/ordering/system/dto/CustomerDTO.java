package com.coffee.ordering.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CustomerDTO {
    private UUID value;
    private String username;
    private String firstName;
    private String lastName;
}
