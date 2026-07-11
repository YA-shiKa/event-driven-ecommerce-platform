package com.ecom.order.controller;

import com.ecom.order.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Powers the live-updating order timeline on the Next.js dashboard. */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SseController {

    private final SseEmitterService sseEmitterService;

    @GetMapping("/stream")
    public SseEmitter streamOrderUpdates() {
        return sseEmitterService.subscribe();
    }
}
