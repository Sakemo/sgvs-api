package com.flick.business.api.controller;

import com.flick.business.api.dto.request.production.ProviderRequest;
import com.flick.business.api.dto.response.production.ProviderResponse;
import com.flick.business.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @GetMapping
    public ResponseEntity<List<ProviderResponse>> listAllProviders() {
        List<ProviderResponse> providers = providerService.listAll();
        return ResponseEntity.ok(providers);
    }

    @PostMapping
    public ResponseEntity<ProviderResponse> createProvider(
            @Valid @RequestBody ProviderRequest request, UriComponentsBuilder uriBuilder) {
        ProviderResponse savedProvider = providerService.save(request);
        URI uri = uriBuilder.path("/api/providers/{id}").buildAndExpand(savedProvider.id()).toUri();
        return ResponseEntity.created(uri).body(savedProvider);
    }
}