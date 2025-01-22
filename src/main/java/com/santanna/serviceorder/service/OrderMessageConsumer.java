package com.santanna.serviceorder.service;

import com.santanna.serviceorder.config.RabbitMqConfig;
import com.santanna.serviceorder.dto.OrderRequestDto;
import com.santanna.serviceorder.handler.model.BadRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OrderMessageConsumer {
    private final OrderService orderService;
    private final Validator validator;

    public OrderMessageConsumer(OrderService orderService, Validator validator) {
        this.orderService = orderService;
        this.validator = validator;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_QUEUE, concurrency = "3-10")
    public void receiveOrder(OrderRequestDto orderRequestDto) {
        try {
            System.out.println("Recebendo pedido da fila: " + orderRequestDto.getOrderNumber());
            Set<ConstraintViolation<OrderRequestDto>> violations = validator.validate(orderRequestDto);

            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder("Validation errors: ");
                for (ConstraintViolation<OrderRequestDto> violation : violations) {
                    sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
                }
                throw new BadRequestException(sb.toString());
            }
            orderService.createOrder(orderRequestDto);
            System.out.println("Pedido processado com sucesso: " + orderRequestDto.getOrderNumber());
        } catch (Exception e) {
            System.err.println("Erro ao processar pedido: " + e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Erro crítico: " + e.getMessage());
        }
    }
}
