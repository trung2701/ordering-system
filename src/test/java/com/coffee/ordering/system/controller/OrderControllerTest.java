package com.coffee.ordering.system.controller;

import com.coffee.ordering.system.common.OrderStatus;
import com.coffee.ordering.system.dataaccess.OrderRepository;
import com.coffee.ordering.system.dataaccess.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private OrderRepository orderRepository;

    private final String PREFIX_URI = "/api/v1/orders";

    @Test
    void createOrder_success() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post(PREFIX_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "d215b5f8-0249-4dc5-89a3-51fd148cfb41",
                                  "coffeeShopId": "d215b5f8-0249-4dc5-89a3-51fd148cfb45",
                                  "address": {
                                    "street": "street_1",
                                    "postalCode": "1000AB",
                                    "city": "Amsterdam"
                                  },
                                  "price": 200.00,
                                  "items": [
                                    {
                                      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",
                                      "quantity": 1,
                                      "price": 50.00,
                                      "subTotal": 50.00
                                    },
                                    {
                                      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb49",
                                      "quantity": 3,
                                      "price": 50.00,
                                      "subTotal": 150.00
                                    }
                                  ]
                                }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.PENDING.name()))
                .andExpect(jsonPath("$.message").value("Order created successfully"));
    }

    @Test
    void getOrderByTrackingId_success() throws Exception {
        OrderEntity orderEntity = OrderEntity.builder().trackingId(UUID.randomUUID()).orderStatus(OrderStatus.PENDING).build();
        when(orderRepository.findByTrackingId(any()))
                .thenReturn(Optional.of(orderEntity));

        mockMvc.perform(MockMvcRequestBuilders.get(PREFIX_URI + "/status/" + orderEntity.getTrackingId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderTrackingId").value(orderEntity.getTrackingId().toString()))
                .andExpect(jsonPath("$.orderStatus").value(orderEntity.getOrderStatus().name()));
    }
}
