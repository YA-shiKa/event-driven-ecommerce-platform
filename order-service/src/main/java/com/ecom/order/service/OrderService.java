package com.ecom.order.service;

import com.ecom.order.dto.CreateOrderRequest;
import com.ecom.order.dto.InventoryEvent;
import com.ecom.order.dto.OrderEvent;
import com.ecom.order.dto.PaymentEvent;
import com.ecom.order.kafka.OrderEventProducer;
import com.ecom.order.model.Order;
import com.ecom.order.model.OrderItem;
import com.ecom.order.model.OrderStatus;
import com.ecom.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerEmail(request.getCustomerEmail());

        BigDecimal total = BigDecimal.ZERO;
        for (var itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            // Demo pricing - a real system would look this up from the Inventory service/catalog.
            item.setUnitPrice(BigDecimal.valueOf(19.99));
            order.addItem(item);
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);

        Order saved = orderRepository.save(order);
        sseEmitterService.broadcast(saved);

        OrderEvent event = new OrderEvent(
                saved.getId(),
                saved.getCustomerEmail(),
                saved.getItems().stream()
                        .map(i -> new OrderEvent.LineItem(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                        .toList(),
                saved.getTotalAmount(),
                Instant.now()
        );
        orderEventProducer.publishOrderCreated(event);

        return saved;
    }

    public List<Order> listOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrder(UUID id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Order not found: " + id));
    }

    @Transactional
    public void handleInventoryEvent(InventoryEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(event.isReserved() ? OrderStatus.INVENTORY_RESERVED : OrderStatus.INVENTORY_REJECTED);
            Order saved = orderRepository.save(order);
            sseEmitterService.broadcast(saved);
            log.info("Order {} inventory status -> {}", order.getId(), order.getStatus());
        });
    }

    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(event.isSuccess() ? OrderStatus.CONFIRMED : OrderStatus.PAYMENT_FAILED);
            Order saved = orderRepository.save(order);
            sseEmitterService.broadcast(saved);
            log.info("Order {} payment status -> {}", order.getId(), order.getStatus());
        });
    }
}
