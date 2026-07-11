package com.ecom.inventory.dto;

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
}
