package com.ecom.payment.kafka;

import com.ecom.payment.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.payment-processed}")
    private String processedTopic;

    @Value("${app.kafka.topic.payment-failed}")
    private String failedTopic;

    public void publish(PaymentEvent event) {
        String topic = event.isSuccess() ? processedTopic : failedTopic;
        log.info("Publishing {} for order {}", topic, event.getOrderId());
        kafkaTemplate.send(topic, event.getOrderId().toString(), event);
    }
}
