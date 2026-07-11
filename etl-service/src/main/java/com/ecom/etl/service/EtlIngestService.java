package com.ecom.etl.service;

import com.ecom.etl.dto.InventoryEvent;
import com.ecom.etl.dto.OrderEvent;
import com.ecom.etl.dto.PaymentEvent;
import com.ecom.etl.model.FactOrderLine;
import com.ecom.etl.repository.FactOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Extract-Transform-Load, in the literal sense:
 *  - EXTRACT: consume raw domain events off Kafka (order.created, inventory.*, payment.*)
 *  - TRANSFORM: flatten a nested OrderEvent into one denormalized row per line item
 *  - LOAD: persist into the analytics-optimized fact_order_line table
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EtlIngestService {

    private final FactOrderLineRepository factOrderLineRepository;

    @Transactional
    public void ingestOrderCreated(OrderEvent event) {
        for (OrderEvent.LineItem item : event.getItems()) {
            FactOrderLine fact = new FactOrderLine();
            fact.setOrderId(event.getOrderId());
            fact.setCustomerEmail(event.getCustomerEmail());
            fact.setProductId(item.getProductId());
            fact.setQuantity(item.getQuantity());
            fact.setUnitPrice(item.getUnitPrice());
            fact.setLineTotal(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
            fact.setOrderStatus("PENDING");
            fact.setOrderCreatedAt(event.getTimestamp());
            factOrderLineRepository.save(fact);
        }
        log.info("ETL loaded {} line item(s) for order {}", event.getItems().size(), event.getOrderId());
    }

    @Transactional
    public void ingestInventoryRejected(InventoryEvent event) {
        updateStatus(event.getOrderId(), "INVENTORY_REJECTED", Instant.now());
    }

    @Transactional
    public void ingestPaymentResult(PaymentEvent event) {
        updateStatus(event.getOrderId(), event.isSuccess() ? "CONFIRMED" : "PAYMENT_FAILED", Instant.now());
    }

    private void updateStatus(java.util.UUID orderId, String status, Instant completedAt) {
        var lines = factOrderLineRepository.findAllByOrderId(orderId);
        for (var line : lines) {
            line.setOrderStatus(status);
            line.setOrderCompletedAt(completedAt);
        }
        factOrderLineRepository.saveAll(lines);
        log.info("ETL updated {} line item(s) for order {} -> {}", lines.size(), orderId, status);
    }
}
