package com.flick.business.service;

import com.flick.business.api.dto.request.production.ProviderRequest;
import com.flick.business.api.dto.response.production.ProviderResponse;
import com.flick.business.core.entity.Provider;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;

    public Provider findById(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + id));
    }

    public List<ProviderResponse> listAll() {
        return providerRepository.findAll().stream()
                .map(provider -> new ProviderResponse(provider.getId(), provider.getName()))
                .collect(Collectors.toList());
    }

    public ProviderResponse save(ProviderRequest request) {
        Provider provider = new Provider();
        provider.setName(request.name());
        Provider savedProvider = providerRepository.save(provider);
        return new ProviderResponse(savedProvider.getId(), savedProvider.getName());
    }
}