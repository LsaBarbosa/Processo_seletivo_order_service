package com.santanna.serviceorder.service;

import com.santanna.serviceorder.domain.Order;
import com.santanna.serviceorder.dto.OrderResponseDto;
import com.santanna.serviceorder.handler.model.NotFoundException;
import com.santanna.serviceorder.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {OrderService.class, ConcurrentMapCacheManager.class})
@EnableCaching
public class OrderServiceCacheTest {
    @Autowired
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @Autowired
    private CacheManager cacheManager;

    private final Long ORDER_ID = 1L;

    @BeforeEach
    void setUp() {
        Order mockOrder = new Order();
        mockOrder.setId(ORDER_ID);

        OrderResponseDto mockResponse = new OrderResponseDto();
        mockResponse.setId(ORDER_ID);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(mockOrder));
    }

    @Test
    void shouldCacheOrderById() {

        OrderResponseDto response1 = orderService.getOrderById(ORDER_ID);
        assertThat(response1).isNotNull();
        assertThat(response1.getId()).isEqualTo(ORDER_ID);

        OrderResponseDto response2 = orderService.getOrderById(ORDER_ID);
        assertThat(response2).isEqualTo(response1);

        verify(orderRepository, times(1)).findById(ORDER_ID);


        var cachedValue = Objects.requireNonNull(cacheManager.getCache("orders")).get(ORDER_ID, OrderResponseDto.class);
        assertThat(cachedValue).isNotNull();
        assertThat(cachedValue.getId()).isEqualTo(ORDER_ID);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        Long invalidOrderId = 99L;
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        try {
            orderService.getOrderById(invalidOrderId);
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("Order not found with ID: " + invalidOrderId);
        }

        verify(orderRepository, times(1)).findById(invalidOrderId);
    }
}
