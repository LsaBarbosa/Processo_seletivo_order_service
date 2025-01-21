package com.santanna.serviceorder.service;

import com.santanna.serviceorder.domain.Order;
import com.santanna.serviceorder.domain.OrderStatus;
import com.santanna.serviceorder.dto.OrderRequestDto;
import com.santanna.serviceorder.dto.OrderResponseDto;
import com.santanna.serviceorder.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        if (orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber()).isPresent()) {
            throw new IllegalArgumentException("Order already exists");
        }
        var order = Order.builder()
                .orderNumber(orderRequestDto.getOrderNumber())
                .productName(orderRequestDto.getProductName())
                .quantity(orderRequestDto.getQuantity())
                .totalValue(orderRequestDto.getUnitPrice()
                        .multiply(BigDecimal.valueOf(orderRequestDto.getQuantity())))
                .orderStatus(OrderStatus.RECEIVED).createdAt(LocalDateTime.now()).build();
        var savedOrder = orderRepository.save(order);
        return toResponseDto(savedOrder);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long id, OrderStatus orderStatus) {
        var order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus(orderStatus);

        var updatedOrder = orderRepository.save(order);
        return toResponseDto(updatedOrder);
    }


    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    private OrderResponseDto toResponseDto(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .totalValue(order.getTotalValue())
                .status(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
