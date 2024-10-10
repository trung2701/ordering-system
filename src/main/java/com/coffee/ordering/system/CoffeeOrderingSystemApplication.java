package com.coffee.ordering.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CoffeeOrderingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeOrderingSystemApplication.class, args);
    }

}
