package com.coffee.ordering.system.configuration.mock;

import com.coffee.ordering.system.shop.v1.api.CoffeeShopApi;
import com.coffee.ordering.system.shop.v1.model.CoffeeShop;
import com.coffee.ordering.system.shop.v1.model.Product;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

public class CoffeeShopMockApi extends CoffeeShopApi {

    public CoffeeShop getShopById(UUID id) throws RestClientException {
        CoffeeShop coffeeShop = new CoffeeShop();
        coffeeShop.setShopId(UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb45"));
        coffeeShop.setActive(true);
        Product productDTO = new Product();
        productDTO.setProductId(UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb48"));
        productDTO.setName("product 1");
        productDTO.setPrice(50.00);

        Product productDTO2 = new Product();
        productDTO2.setProductId(UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb49"));
        productDTO2.setName("product 2");
        productDTO2.setPrice(50.00);

        coffeeShop.setProducts(List.of(productDTO, productDTO2));
        return coffeeShop;
    }
}
