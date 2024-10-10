package com.coffee.ordering.system.application.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponse {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
}
