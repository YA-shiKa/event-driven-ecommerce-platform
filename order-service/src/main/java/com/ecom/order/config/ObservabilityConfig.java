package com.ecom.order.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

/**
 * Keeps Jaeger focused on real business traffic: Prometheus scrapes
 * /actuator/prometheus
 * every few seconds, and without this every scrape shows up as its own trace
 * and buries
 * the order/payment/inventory traces you actually want to look at.
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public ObservationPredicate noActuatorObservations() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                return !serverContext.getCarrier().getRequestURI().startsWith("/actuator");
            }
            return true;
        };
    }
}