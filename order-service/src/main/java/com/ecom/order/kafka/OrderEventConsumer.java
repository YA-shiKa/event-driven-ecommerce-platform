package com.ecom.order.kafka;

import com.ecom.order.dto.InventoryEvent;
import com.ecom.order.dto.PaymentEvent;
import com.ecom.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Closes the order saga loop: the Order service reacts to downstream events
 * from Inventory and Payment to move an order to its final state.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = {"inventory.reserved", "inventory.rejected"},
            containerFactory = "inventoryEventListenerContainerFactory")
    public void onInventoryEvent(InventoryEvent event) {
        log.info("Received inventory event for order {}: reserved={}", event.getOrderId(), event.isReserved());
        orderService.handleInventoryEvent(event);
    }

    @KafkaListener(topics = {"payment.processed", "payment.failed"},
            containerFactory = "paymentEventListenerContainerFactory")
    public void onPaymentEvent(PaymentEvent event) {
        log.info("Received payment event for order {}: success={}", event.getOrderId(), event.isSuccess());
        orderService.handlePaymentEvent(event);
    }
}
