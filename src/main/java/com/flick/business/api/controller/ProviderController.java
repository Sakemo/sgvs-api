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
    public ResponseEntity<List<ProviderResponse>> listAllProviders(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String orderBy) {
        List<ProviderResponse> providers = providerService.listAll(name, orderBy);
        return ResponseEntity.ok(providers);
    }

    @PostMapping
    public ResponseEntity<ProviderResponse> createProvider(
            @Valid @RequestBody ProviderRequest request, UriComponentsBuilder uriBuilder) {
        ProviderResponse savedProvider = providerService.save(request);
        URI uri = uriBuilder.path("/api/providers/{id}").buildAndExpand(savedProvider.id()).toUri();
        return ResponseEntity.created(uri).body(savedProvider);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> findProviderById(@PathVariable Long id) {
        ProviderResponse provider = providerService.findById(id);
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProviderResponse> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody ProviderRequest request) {
        ProviderResponse updatedProvider = providerService.update(id, request);
        return ResponseEntity.ok(updatedProvider);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        providerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<ProviderResponse> copyProvider(@PathVariable Long id, UriComponentsBuilder uriBuilder) {
        ProviderResponse copiedProvider = providerService.copy(id);
        URI uri = uriBuilder.path("/api/providers/{id}").buildAndExpand(copiedProvider.id()).toUri();
        return ResponseEntity.created(uri).body(copiedProvider);
    }
}
