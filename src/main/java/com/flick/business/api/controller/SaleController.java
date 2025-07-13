package com.flick.business.api.controller;

import com.flick.business.api.dto.request.SaleRequest;
import com.flick.business.api.dto.response.SaleResponse;
import com.flick.business.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponse> registerSale(
            @Valid @RequestBody SaleRequest request,
            UriComponentsBuilder uriBuilder) {
        SaleResponse savedSale = saleService.registerSale(request);
        URI uri = uriBuilder.path("/api/sales/{id}").buildAndExpand(savedSale.id()).toUri();
        return ResponseEntity.created(uri).body(savedSale);
    }

    @GetMapping
    public ResponseEntity<Page<SaleResponse>> listSales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String orderBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SaleResponse> salesPage = saleService.listAll(startDate, endDate, customerId, paymentMethod, productId,
                orderBy, page, size);
        return ResponseEntity.ok(salesPage);
    }
}