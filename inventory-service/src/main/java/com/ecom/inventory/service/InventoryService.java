package com.ecom.inventory.service;

import com.ecom.inventory.dto.InventoryEvent;
import com.ecom.inventory.dto.OrderEvent;
import com.ecom.inventory.kafka.InventoryEventProducer;
import com.ecom.inventory.model.Product;
import com.ecom.inventory.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryEventProducer inventoryEventProducer;

    @Transactional
    public void reserveStock(OrderEvent orderEvent) {
        boolean allAvailable = true;
        String rejectionReason = null;

        try {
            for (OrderEvent.LineItem item : orderEvent.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElse(null);

                if (product == null || product.getAvailableQuantity() < item.getQuantity()) {
                    allAvailable = false;
                    rejectionReason = "Insufficient stock for product " + item.getProductId();
                    break;
                }
            }

            if (allAvailable) {
                // Second pass: actually decrement, now that we know everything is available.
                // @Version on Product guards against lost updates from concurrent orders.
                for (OrderEvent.LineItem item : orderEvent.getItems()) {
                    Product product = productRepository.findById(item.getProductId()).orElseThrow();
                    product.setAvailableQuantity(product.getAvailableQuantity() - item.getQuantity());
                    productRepository.save(product);
                }
            }
        } catch (OptimisticLockException e) {
            allAvailable = false;
            rejectionReason = "Concurrent reservation conflict, please retry";
        }

        InventoryEvent result = new InventoryEvent(orderEvent.getOrderId(), allAvailable, rejectionReason, Instant.now());
        inventoryEventProducer.publish(result);
    }
}
