package com.flick.business.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flick.business.api.dto.request.commercial.PaymentRequest;
import com.flick.business.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Void> createPayment(@Valid @RequestBody PaymentRequest request) {
        paymentService.recordPayment(request);
        return ResponseEntity.noContent().build();
    }
}
