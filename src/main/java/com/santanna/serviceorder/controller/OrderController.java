package com.santanna.serviceorder.controller;

import com.santanna.serviceorder.domain.OrderStatus;
import com.santanna.serviceorder.dto.OrderRequestDto;
import com.santanna.serviceorder.dto.OrderResponseDto;
import com.santanna.serviceorder.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        var createOrder = orderService.createOrder(orderRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrder);
    }

    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OrderResponseDto> updateStatus(@Valid @PathVariable Long id, @RequestParam OrderStatus orderStatus) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, orderStatus));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
