package com.ecom.payment.service;

import com.ecom.payment.dto.InventoryEvent;
import com.ecom.payment.dto.PaymentEvent;
import com.ecom.payment.kafka.PaymentEventProducer;
import com.ecom.payment.model.Payment;
import com.ecom.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Value("${app.payment.simulated-failure-rate}")
    private double simulatedFailureRate;

    @Transactional
    public void processPayment(InventoryEvent inventoryEvent) {
        // Only orders with reserved stock reach payment.
        if (!inventoryEvent.isReserved()) {
            return;
        }

        // Demo gateway call: succeeds most of the time, occasionally fails so the
        // saga's compensating path (order -> PAYMENT_FAILED) is actually exercised.
        boolean success = ThreadLocalRandom.current().nextDouble() > simulatedFailureRate;
        BigDecimal amount = BigDecimal.valueOf(19.99); // demo: real amount would travel with the event

        Payment payment = new Payment();
        payment.setOrderId(inventoryEvent.getOrderId());
        payment.setAmount(amount);
        payment.setSuccessful(success);
        payment.setFailureReason(success ? null : "Card declined by simulated gateway");
        paymentRepository.save(payment);

        PaymentEvent event = new PaymentEvent(
                inventoryEvent.getOrderId(),
                success,
                amount,
                payment.getFailureReason(),
                Instant.now()
        );
        paymentEventProducer.publish(event);
    }
}
