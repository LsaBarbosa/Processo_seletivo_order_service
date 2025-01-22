package com.santanna.serviceorder.controller;

import com.santanna.serviceorder.domain.OrderStatus;
import com.santanna.serviceorder.dto.OrderRequestDto;
import com.santanna.serviceorder.dto.OrderResponseDto;
import com.santanna.serviceorder.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(Pageable pageable) {
        Page<OrderResponseDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        var order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

}
