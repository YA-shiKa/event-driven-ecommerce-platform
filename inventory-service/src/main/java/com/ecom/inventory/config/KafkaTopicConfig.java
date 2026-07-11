package com.ecom.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.inventory-reserved}")
    private String reservedTopic;

    @Value("${app.kafka.topic.inventory-rejected}")
    private String rejectedTopic;

    @Bean
    public NewTopic inventoryReservedTopic() {
        return TopicBuilder.name(reservedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryRejectedTopic() {
        return TopicBuilder.name(rejectedTopic).partitions(3).replicas(1).build();
    }
}
