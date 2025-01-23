package com.santanna.serviceorder.app.controller;

import com.santanna.serviceorder.domain.OrderStatus;
import com.santanna.serviceorder.domain.dto.OrderRequestDto;
import com.santanna.serviceorder.domain.dto.OrderResponseDto;
import com.santanna.serviceorder.domain.service.OrderService;
import com.santanna.serviceorder.utils.LoggerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order Controller", description = "Gerenciamento de pedidos")
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final LoggerUtils loggerUtils;

    public OrderController(OrderService orderService, LoggerUtils loggerUtils) {
        this.orderService = orderService;
        this.loggerUtils = loggerUtils;
    }

    @Operation(summary = "Cria um novo pedido", description = "Cria um novo pedido")
    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso")
    @ApiResponse(responseCode = "404", description = "Dados inválidos")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        loggerUtils.logInfo(OrderController.class, "Received request to create an order: {}", orderRequestDto.getOrderNumber());

        OrderResponseDto createdOrder = orderService.createOrder(orderRequestDto);

        loggerUtils.logInfo(OrderController.class, "Order successfully created with ID: {}", createdOrder.getId());
        return ResponseEntity.status(201).body(createdOrder);
    }

    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido existente")
    @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OrderResponseDto> updateStatus(@Valid @PathVariable Long id, @RequestParam OrderStatus orderStatus) {
        loggerUtils.logInfo(OrderController.class, "Received request to update order status. ID: {}, New Status: {}", id, orderStatus);
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(id, orderStatus);

        loggerUtils.logInfo(OrderController.class, "Order status updated successfully. ID: {}, New Status: {}", id, updatedOrder.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Listar pedidos", description = "Lista todos os pedidos com suporte a paginação")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(Pageable pageable) {
        loggerUtils.logInfo(OrderController.class, "Received request to retrieve all orders with pagination");

        Page<OrderResponseDto> orders = orderService.getAllOrders(pageable);
        loggerUtils.logInfo(OrderController.class, "Successfully retrieved {} orders", orders.getTotalElements());

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Buscar um pedido por ID", description = "Retorna os detalhes de um pedido específico")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        loggerUtils.logInfo(OrderController.class, "Received request to fetch order by ID: {}", id);

        OrderResponseDto order = orderService.getOrderById(id);

        loggerUtils.logInfo(OrderController.class, "Order retrieved successfully. ID: {}", order.getId());
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Excluir um pedido", description = "Remove um pedido pelo ID")
    @ApiResponse(responseCode = "204", description = "Pedido excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        loggerUtils.logWarn(OrderController.class, "Received request to delete order with ID: {}", id);

        orderService.deleteOrder(id);

        loggerUtils.logInfo(OrderController.class, "Order with ID {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

}
