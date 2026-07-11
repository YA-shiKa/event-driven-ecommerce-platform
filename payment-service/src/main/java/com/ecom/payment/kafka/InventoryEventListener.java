package com.ecom.payment.kafka;

import com.ecom.payment.dto.InventoryEvent;
import com.ecom.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${app.kafka.topic.inventory-reserved}",
            containerFactory = "inventoryEventListenerContainerFactory")
    public void onInventoryReserved(InventoryEvent event) {
        log.info("Received inventory.reserved for order {}, charging payment", event.getOrderId());
        paymentService.processPayment(event);
    }
}
