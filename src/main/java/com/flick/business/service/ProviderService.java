package com.flick.business.service;

import com.flick.business.core.entity.Provider;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;

    public Provider findById(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + id));
    }
}