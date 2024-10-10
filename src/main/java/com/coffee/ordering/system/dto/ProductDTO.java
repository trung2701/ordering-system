package com.coffee.ordering.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class ProductDTO {
    private UUID productId;
    private String name;
    private BigDecimal price;

    public void alignWithShop(String name, Double price) {
        this.name = name;
        this.price = BigDecimal.valueOf(price).setScale(2, RoundingMode.UNNECESSARY);
    }
}
