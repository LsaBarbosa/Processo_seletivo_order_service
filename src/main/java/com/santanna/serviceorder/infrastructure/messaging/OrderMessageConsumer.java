package com.santanna.serviceorder.infrastructure.messaging;

import com.santanna.serviceorder.infrastructure.config.RabbitMqConfig;
import com.santanna.serviceorder.domain.dto.OrderRequestDto;
import com.santanna.serviceorder.app.handler.model.BadRequestException;
import com.santanna.serviceorder.domain.service.OrderService;
import com.santanna.serviceorder.utils.LoggerUtils;
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
    private final LoggerUtils loggerUtils;

    public OrderMessageConsumer(OrderService orderService, Validator validator, LoggerUtils loggerUtils) {
        this.orderService = orderService;
        this.validator = validator;
        this.loggerUtils = loggerUtils;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_QUEUE, concurrency = "3-10")
    public void receiveOrder(OrderRequestDto orderRequestDto) {
        try {
            loggerUtils.logInfo(OrderMessageConsumer.class, "Received new order message from queue. Order number: {}", orderRequestDto.getOrderNumber());

            Set<ConstraintViolation<OrderRequestDto>> violations = validator.validate(orderRequestDto);

            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder("Validation errors: ");
                for (ConstraintViolation<OrderRequestDto> violation : violations) {
                    sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
                }
                loggerUtils.logWarn(OrderMessageConsumer.class, "Validation failed for order number {}: {}", orderRequestDto.getOrderNumber(), sb.toString());

                throw new BadRequestException(sb.toString());
            }
            orderService.createOrder(orderRequestDto);
            loggerUtils.logInfo(OrderMessageConsumer.class, "Order successfully processed. Order number: {}", orderRequestDto.getOrderNumber());

        } catch (Exception e) {
            loggerUtils.logWarn(OrderMessageConsumer.class, "Bad request error while processing order number {}: {}", orderRequestDto.getOrderNumber(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Erro crítico: " + e.getMessage());
        }
    }
}
