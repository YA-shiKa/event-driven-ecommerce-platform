package com.ecom.order.kafka;

import com.ecom.order.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.order-created}")
    private String orderCreatedTopic;

    public void publishOrderCreated(OrderEvent event) {
        log.info("Publishing order.created for orderId={}", event.getOrderId());
        // Key by orderId so all events for an order land on the same partition,
        // preserving per-order ordering guarantees.
        kafkaTemplate.send(orderCreatedTopic, event.getOrderId().toString(), event);
    }
}
