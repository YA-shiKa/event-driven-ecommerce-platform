package com.ecom.inventory.config;

import com.ecom.inventory.model.Product;
import com.ecom.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Seeds a small product catalog on startup so the demo works out of the box. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        seed("sku-widget-001", "Standard Widget", 50);
        seed("sku-widget-002", "Deluxe Widget", 20);
        seed("sku-gadget-001", "Pocket Gadget", 5);
        seed("sku-gadget-002", "Pro Gadget", 0);
    }

    private void seed(String id, String name, int qty) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setAvailableQuantity(qty);
        productRepository.save(p);
    }
}
