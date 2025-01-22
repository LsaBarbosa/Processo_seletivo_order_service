package com.santanna.serviceorder.service;

import com.santanna.serviceorder.domain.Order;
import com.santanna.serviceorder.domain.OrderStatus;
import com.santanna.serviceorder.dto.OrderRequestDto;
import com.santanna.serviceorder.dto.OrderResponseDto;
import com.santanna.serviceorder.handler.model.BadRequestException;
import com.santanna.serviceorder.handler.model.InternalServerErrorException;
import com.santanna.serviceorder.handler.model.NotFoundException;
import com.santanna.serviceorder.repository.OrderRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.testcontainers.containers.GenericContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableCaching
@AutoConfigureCache
class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    private Order order;
    private OrderRequestDto orderRequestDto;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;


    static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7.0.5").withExposedPorts(6379);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        redisContainer.start();
        System.setProperty("spring.redis.host", redisContainer.getHost());
        System.setProperty("spring.redis.port", redisContainer.getFirstMappedPort().toString());

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
    @DisplayName("Should Throw BadRequestException when order already exists")
    void shouldThrowBadRequestException_WhenOrderAlreadyExists() {
        when(orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber())).thenReturn(Optional.of(new Order()));

        var exception = assertThrows(BadRequestException.class, () -> orderService.createOrder(orderRequestDto));
        assertEquals("Order already exists", exception.getMessage());
    }

    @Test
    @DisplayName("Should Throw BadRequestException when validation fails")
    void shouldThrowBadRequestException_WhenValidationFails() {
        when(orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenThrow(new ConstraintViolationException("Validation Error", null));

        var exception = assertThrows(BadRequestException.class, () -> orderService.createOrder(orderRequestDto));
        assertTrue(exception.getMessage().contains("Validation failed"));
    }

    @Test
    @DisplayName("Should Throw BadRequestException on data integrity violation")
    void shouldThrowBadRequestException_OnDataIntegrityViolation() {
        when(orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenThrow(new DataIntegrityViolationException("Constraint Error"));

        var exception = assertThrows(BadRequestException.class, () -> orderService.createOrder(orderRequestDto));
        assertTrue(exception.getMessage().contains("Database integrity violation"));
    }

    @Test
    @DisplayName("Should Throw InternalServerErrorException on unexpected error")
    void shouldThrowInternalServerErrorException_OnUnexpectedError() {
        when(orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Unexpected error"));

        var exception = assertThrows(InternalServerErrorException.class, () -> orderService.createOrder(orderRequestDto));
        assertEquals("Unexpected error occurred while creating order.", exception.getMessage());
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
    @DisplayName("Should Throw InternalServerErrorException when unexpected error updating order")
    void shouldThrowInternalServerErrorException_WhenUnexpectedErrorUpdatingOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Unexpected save error"));

        var exception = assertThrows(InternalServerErrorException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.PROCESSED));
        assertEquals("Unexpected error while updating order status.", exception.getMessage());
    }

    @Test
    @DisplayName("Should Get All Orders With Pagination Successfully")
    void shouldGetAllOrdersWithPaginationSuccessfully() {
        var pageable = Pageable.ofSize(10);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(new Order(), new Order())));

        Page<OrderResponseDto> response = orderService.getAllOrders(pageable);
        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should Get Order By ID Successfully")
    void shouldGetOrderByIdSuccessfully() {
        Order order = new Order();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponseDto response = orderService.getOrderById(1L);
        assertNotNull(response);
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should Throw NotFoundException When Order ID Not Found")
    void shouldThrowNotFoundExceptionWhenOrderIdNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> orderService.getOrderById(1L));
        assertEquals("Order not found with ID: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Should Delete Order Successfully")
    void shouldDeleteOrderSuccessfully() {
        Order order = new Order();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        orderService.deleteOrder(1L);
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    @DisplayName("Should Throw NotFoundException When Deleting Order That Does Not Exist")
    void shouldThrowNotFoundExceptionWhenDeletingOrderThatDoesNotExist() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> orderService.deleteOrder(1L));
        assertEquals("Order not found with ID: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Should Get Order By ID and Cache the Result")
    void shouldGetOrderByIdAndCacheResult() {
        Long orderId = 1L;
        OrderResponseDto mockOrder = new OrderResponseDto();
        mockOrder.setId(orderId);
        mockOrder.setProductName("Produto Teste");
        mockOrder.setTotalValue(new BigDecimal("300.00"));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        var valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("orders::" + orderId)).thenReturn(null);

        OrderResponseDto result1 = orderService.getOrderById(orderId);
        assertNotNull(result1);
        assertEquals(orderId, result1.getId());
        verify(orderRepository, times(1)).findById(orderId);

        when(valueOperations.get("orders::" + orderId)).thenReturn(mockOrder);
        OrderResponseDto result2 = orderService.getOrderById(orderId);
        assertNotNull(result2);
        assertEquals("Produto Teste", result2.getProductName());
        verify(orderRepository, times(2)).findById(orderId);
    }
}