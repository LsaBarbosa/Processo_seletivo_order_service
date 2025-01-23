package com.santanna.serviceorder.domain.model;

import com.santanna.serviceorder.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders",indexes = {
        @Index(name = "idx_order_number", columnList = "orderNumber"),
        @Index(name = "idx_order_status", columnList = "orderStatus")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    private String productName;

    private Integer quantity;

    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime createdAt;
}
