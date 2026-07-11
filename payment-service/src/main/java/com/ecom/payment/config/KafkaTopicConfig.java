package com.ecom.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.payment-processed}")
    private String processedTopic;

    @Value("${app.kafka.topic.payment-failed}")
    private String failedTopic;

    @Bean
    public NewTopic paymentProcessedTopic() {
        return TopicBuilder.name(processedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(failedTopic).partitions(3).replicas(1).build();
    }
}
