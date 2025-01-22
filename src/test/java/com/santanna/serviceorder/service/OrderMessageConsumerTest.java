package com.santanna.serviceorder.service;

import com.santanna.serviceorder.dto.OrderRequestDto;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OrderMessageConsumerTest {
    @InjectMocks
    private OrderMessageConsumer orderMessageConsumer;

    @Mock
    private OrderService orderService;

    @Mock
    private Validator validator;

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
    }

    @Test
    @DisplayName("Should process order successfully")
    void shouldProcessOrderSuccessfully() {
        when(validator.validate(orderRequestDto)).thenReturn(Collections.emptySet());

        assertDoesNotThrow(() -> orderMessageConsumer.receiveOrder(orderRequestDto));
        verify(orderService, times(1)).createOrder(orderRequestDto);
    }

    @Test
    @DisplayName("Should throw AmqpRejectAndDontRequeueException on unexpected error")
    void shouldThrowAmqpRejectAndDontRequeueExceptionOnUnexpectedError() {
        when(validator.validate(orderRequestDto)).thenReturn(Collections.emptySet());
        doThrow(new RuntimeException("Unexpected error"))
                .when(orderService).createOrder(orderRequestDto);

        var exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> orderMessageConsumer.receiveOrder(orderRequestDto));
        assertEquals("Erro crítico: Unexpected error", exception.getMessage());
    }

}