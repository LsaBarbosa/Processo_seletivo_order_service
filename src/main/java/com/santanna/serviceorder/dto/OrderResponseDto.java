package com.santanna.serviceorder.dto;

import com.santanna.serviceorder.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponseDto {
    private Long id;
    private String orderNumber;
    private String productName;
    private Integer quantity;
    private BigDecimal totalValue;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
