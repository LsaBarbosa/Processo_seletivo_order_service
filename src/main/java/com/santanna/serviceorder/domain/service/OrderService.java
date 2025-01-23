package com.santanna.serviceorder.domain.service;

import com.santanna.serviceorder.domain.model.Order;
import com.santanna.serviceorder.domain.OrderStatus;
import com.santanna.serviceorder.domain.dto.OrderRequestDto;
import com.santanna.serviceorder.domain.dto.OrderResponseDto;
import com.santanna.serviceorder.app.handler.model.BadRequestException;
import com.santanna.serviceorder.app.handler.model.InternalServerErrorException;
import com.santanna.serviceorder.app.handler.model.NotFoundException;
import com.santanna.serviceorder.infrastructure.repository.OrderRepository;
import com.santanna.serviceorder.utils.LoggerUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {
    private final LoggerUtils loggerUtils;
    private final OrderRepository orderRepository;

    public OrderService(LoggerUtils loggerUtils, OrderRepository orderRepository) {
        this.loggerUtils = loggerUtils;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponseDto createOrder( OrderRequestDto orderRequestDto) {
        loggerUtils.logInfo(OrderService.class, "Starting order creation: {}", orderRequestDto.getOrderNumber());

        boolean numberIsPresent = orderRepository.findByOrderNumber(orderRequestDto.getOrderNumber()).isPresent();
        if (numberIsPresent) {
            loggerUtils.logWarn(OrderService.class, "Duplicate order detected: {}", orderRequestDto.getOrderNumber());
            throw new BadRequestException("Order already exists");
        }
        try {

            var order = Order.builder()
                    .orderNumber(orderRequestDto.getOrderNumber())
                    .productName(orderRequestDto.getProductName())
                    .quantity(orderRequestDto.getQuantity())
                    .totalValue(orderRequestDto.getUnitPrice()
                            .multiply(BigDecimal.valueOf(orderRequestDto.getQuantity())))
                    .orderStatus(OrderStatus.PROCESSED).createdAt(LocalDateTime.now()).build();
            var savedOrder = orderRepository.save(order);

            loggerUtils.logInfo(OrderService.class, "Order created successfully. ID: {}", savedOrder.getId());
            return toResponseDto(savedOrder);

        } catch (ConstraintViolationException ex) {
            loggerUtils.logError(OrderService.class, "Validation failed while creating order: {}", ex, orderRequestDto);
            throw new BadRequestException("Validation failed: " + ex.getMessage());

        } catch (DataIntegrityViolationException ex) {
            loggerUtils.logError(OrderService.class, "Database integrity violation while creating order: {}", ex, orderRequestDto);
            throw new BadRequestException("Database integrity violation: " + ex.getMessage());

        } catch (Exception ex) {
            loggerUtils.logError(OrderService.class, "Unexpected error while creating order: {}", ex, orderRequestDto);
            throw new InternalServerErrorException("Unexpected error occurred while creating order.");
        }
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#id",  allEntries =true)
    public OrderResponseDto updateOrderStatus(Long id, OrderStatus orderStatus) {
        loggerUtils.logInfo(OrderService.class, "Updating order status. ID: {}, New Status: {}", id, orderStatus);

        try {
            var order = orderRepository.findById(id).orElseThrow(() -> {
                loggerUtils.logWarn(OrderService.class, "Order with ID {} not found", id);
                return new NotFoundException("Order not found");
            });
            order.setOrderStatus(orderStatus);

            var updatedOrder = orderRepository.save(order);
            loggerUtils.logInfo(OrderService.class, "Order status updated successfully. ID: {}, New Status: {}", id, orderStatus);

            return toResponseDto(updatedOrder);

        } catch (DataIntegrityViolationException ex) {
            loggerUtils.logError(OrderService.class, "Invalid data for updating order status: {}", ex, id);
            throw new BadRequestException("Invalid data for updating order status: " + ex.getMessage());

        } catch (Exception ex) {
            loggerUtils.logError(OrderService.class, "Unexpected error while updating order status: {}", ex, id);
            throw new InternalServerErrorException("Unexpected error while updating order status.");
        }
    }

    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        loggerUtils.logInfo(OrderService.class, "Fetching all orders with pagination");

        Page<OrderResponseDto> orders = orderRepository.findAll(pageable)
                .map(this::toResponseDto);

        loggerUtils.logInfo(OrderService.class, "Retrieved {} orders successfully", orders.getTotalElements());
        return orders;
    }

    @Cacheable(value = "orders", key = "#id")
    public OrderResponseDto getOrderById(Long id) {
        loggerUtils.logInfo(OrderService.class, "Fetching order by ID: {}", id);

        var order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    loggerUtils.logWarn(OrderService.class, "Order with ID {} not found", id);
                    return new NotFoundException("Order not found with ID: " + id);
                });

        loggerUtils.logInfo(OrderService.class, "Order found. ID: {}", id);
        return toResponseDto(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        loggerUtils.logInfo(OrderService.class, "Deleting order with ID: {}", id);

        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + id));

        orderRepository.delete(order);
        loggerUtils.logInfo(OrderService.class, "Order with ID {} deleted successfully", id);
    }

    private OrderResponseDto toResponseDto(Order order) {
        loggerUtils.logDebug(OrderService.class, "Converting Order entity to DTO. ID: {}", order.getId());
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
