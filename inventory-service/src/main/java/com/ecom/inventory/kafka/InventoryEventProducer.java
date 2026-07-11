package com.ecom.inventory.kafka;

import com.ecom.inventory.dto.InventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.inventory-reserved}")
    private String reservedTopic;

    @Value("${app.kafka.topic.inventory-rejected}")
    private String rejectedTopic;

    public void publish(InventoryEvent event) {
        String topic = event.isReserved() ? reservedTopic : rejectedTopic;
        log.info("Publishing {} for order {}", topic, event.getOrderId());
        kafkaTemplate.send(topic, event.getOrderId().toString(), event);
    }
}
