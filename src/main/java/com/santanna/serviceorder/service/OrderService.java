package com.santanna.serviceorder.service;

import com.santanna.serviceorder.domain.Order;
import com.santanna.serviceorder.domain.OrderStatus;
import com.santanna.serviceorder.dto.OrderRequestDto;
import com.santanna.serviceorder.dto.OrderResponseDto;
import com.santanna.serviceorder.handler.model.BadRequestException;
import com.santanna.serviceorder.handler.model.InternalServerErrorException;
import com.santanna.serviceorder.handler.model.NotFoundException;
import com.santanna.serviceorder.repository.OrderRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponseDto createOrder( OrderRequestDto orderRequestDto) {
        boolean numberIsPresent = orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber()).isPresent();
        if (numberIsPresent) throw new BadRequestException("Order already exists");
        try {
            var order = Order.builder()
                    .orderNumber(orderRequestDto.getOrderNumber())
                    .productName(orderRequestDto.getProductName())
                    .quantity(orderRequestDto.getQuantity())
                    .totalValue(orderRequestDto.getUnitPrice()
                            .multiply(BigDecimal.valueOf(orderRequestDto.getQuantity())))
                    .orderStatus(OrderStatus.PROCESSED).createdAt(LocalDateTime.now()).build();
            var savedOrder = orderRepository.save(order);
            return toResponseDto(savedOrder);

        } catch (ConstraintViolationException ex) {
            throw new BadRequestException("Validation failed: " + ex.getMessage());

        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Database integrity violation: " + ex.getMessage());

        } catch (Exception ex) {
            throw new InternalServerErrorException("Unexpected error occurred while creating order.");
        }
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long id, OrderStatus orderStatus) {
        try {
            var order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
            order.setOrderStatus(orderStatus);

            var updatedOrder = orderRepository.save(order);
            return toResponseDto(updatedOrder);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Invalid data for updating order status: " + ex.getMessage());

        } catch (Exception ex) {
            throw new InternalServerErrorException("Unexpected error while updating order status.");
        }
    }

    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::toResponseDto);
    }


    public OrderResponseDto getOrderById(Long id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + id));
        return toResponseDto(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + id));

        orderRepository.delete(order);
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
