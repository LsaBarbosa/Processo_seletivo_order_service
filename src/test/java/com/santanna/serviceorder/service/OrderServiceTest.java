package com.santanna.serviceorder.service;

import com.santanna.serviceorder.domain.Order;
import com.santanna.serviceorder.domain.OrderStatus;
import com.santanna.serviceorder.dto.OrderRequestDto;
import com.santanna.serviceorder.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    private Order order;
    private OrderRequestDto orderRequestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        orderRequestDto = OrderRequestDto.builder()
                .orderNumber("ORD12345")
                .productName("Produto Teste")
                .quantity(3)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber("ORD12345")
                .productName("Produto Teste")
                .quantity(3)
                .totalValue(new BigDecimal("300.00"))
                .orderStatus(OrderStatus.RECEIVED)
                .build();
    }


    @Test
    @DisplayName("Should Create Order Success")
    void shouldCreateOrder_Success() {
        when(orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        var responseDto = orderService.createOrder(orderRequestDto);

        assertNotNull(responseDto);
        assertEquals("ORD12345", responseDto.getOrderNumber());
        assertEquals(new BigDecimal("300.00"), responseDto.getTotalValue());
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    @Test
    @DisplayName("Should Not Create Order Already Exists")
    void shouldNotCreateOrder_AlreadyExists() {
        when(orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber())).thenReturn(Optional.of(order));

        var exception = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderRequestDto));

        assertEquals("Order already exists", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should Update Order Status Success")
    void shouldUpdateOrderStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        var responseDto = orderService.updateOrderStatus(1L, OrderStatus.PROCESSED);

        assertNotNull(responseDto);
        assertEquals(OrderStatus.PROCESSED, responseDto.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Should Not Update Order Status Not Found")
    void shouldUpdateNotOrderStatus_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.PROCESSED));

        assertEquals("Order not found", exception.getMessage());
    }
}