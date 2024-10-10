package com.coffee.ordering.system.test;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/customer", produces = "application/json")
public class CustomerServiceSimulator {

    private final CustomerProducer customerProducer;

    @PostMapping
    void createCustomer() {
        customerProducer.produce();
    }
}
