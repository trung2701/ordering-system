package com.coffee.ordering.system.configuration;

import com.coffee.ordering.system.configuration.mock.CoffeeShopMockApi;
import com.coffee.ordering.system.shop.v1.ApiClient;
import com.coffee.ordering.system.shop.v1.api.CoffeeShopApi;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ClientConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "order-service", name = "local-config", havingValue = "false")
    public CoffeeShopApi coffeeShopApi() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("http://localhost:8080/");
        return new CoffeeShopApi(apiClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "order-service", name = "local-config", havingValue = "true")
    public CoffeeShopApi mockCoffeeShopApi() {
        return new CoffeeShopMockApi();
    }
}
