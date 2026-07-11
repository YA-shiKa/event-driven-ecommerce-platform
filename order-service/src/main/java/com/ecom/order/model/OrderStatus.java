package com.ecom.order.model;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    INVENTORY_REJECTED,
    PAID,
    PAYMENT_FAILED,
    CONFIRMED,
    CANCELLED
}
