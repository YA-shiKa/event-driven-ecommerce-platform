package com.ecom.etl.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One row per order line item - the grain of this fact table.
 * Denormalized on purpose (customer email, order status duplicated per line) so
 * analytics queries don't need joins back to the transactional order-service DB,
 * which the ETL pipeline intentionally has no direct access to.
 */
@Entity
@Table(name = "fact_order_line")
@Getter
@Setter
public class FactOrderLine {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal lineTotal;

    @Column(nullable = false)
    private String orderStatus; // PENDING, CONFIRMED, PAYMENT_FAILED, INVENTORY_REJECTED

    @Column(nullable = false)
    private Instant orderCreatedAt;

    private Instant orderCompletedAt;
}
