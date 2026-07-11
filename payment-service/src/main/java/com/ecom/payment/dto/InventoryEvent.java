package com.ecom.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {
    private UUID orderId;
    private boolean reserved;
    private String reason;
    private Instant timestamp;
    // Note: a production version would carry the order total here so payment-service
    // doesn't need a separate lookup. Kept simple for the demo - amount is randomized.
}
