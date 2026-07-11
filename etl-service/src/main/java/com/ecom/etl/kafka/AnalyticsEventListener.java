package com.ecom.etl.kafka;

import com.ecom.etl.dto.InventoryEvent;
import com.ecom.etl.dto.OrderEvent;
import com.ecom.etl.dto.PaymentEvent;
import com.ecom.etl.service.EtlIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {

    private final EtlIngestService etlIngestService;

    @KafkaListener(topics = "${app.kafka.topic.order-created}",
            containerFactory = "orderEventListenerContainerFactory")
    public void onOrderCreated(OrderEvent event) {
        etlIngestService.ingestOrderCreated(event);
    }

    @KafkaListener(topics = "${app.kafka.topic.inventory-rejected}",
            containerFactory = "inventoryEventListenerContainerFactory")
    public void onInventoryRejected(InventoryEvent event) {
        etlIngestService.ingestInventoryRejected(event);
    }

    @KafkaListener(topics = {"${app.kafka.topic.payment-processed}", "${app.kafka.topic.payment-failed}"},
            containerFactory = "paymentEventListenerContainerFactory")
    public void onPaymentResult(PaymentEvent event) {
        etlIngestService.ingestPaymentResult(event);
    }
}
