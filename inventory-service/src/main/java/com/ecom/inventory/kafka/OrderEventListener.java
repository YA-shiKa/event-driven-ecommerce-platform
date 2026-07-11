package com.ecom.inventory.kafka;

import com.ecom.inventory.dto.OrderEvent;
import com.ecom.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "${app.kafka.topic.order-created}",
            containerFactory = "orderEventListenerContainerFactory")
    public void onOrderCreated(OrderEvent event) {
        log.info("Received order.created for order {}, checking stock", event.getOrderId());
        inventoryService.reserveStock(event);
    }
}
