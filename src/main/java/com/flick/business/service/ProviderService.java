package com.flick.business.service;

import com.flick.business.api.dto.request.production.ProviderRequest;
import com.flick.business.api.dto.response.production.ProviderResponse;
import com.flick.business.core.entity.Provider;
import com.flick.business.core.entity.security.User;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.ProductRepository;
import com.flick.business.repository.ProviderRepository;
import com.flick.business.service.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private static final Pattern COPY_SUFFIX_PATTERN = Pattern.compile(" - Copy \\((\\d+)\\)$");

    private final ProviderRepository providerRepository;
    private final ProductRepository productRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional(readOnly = true)
    public ProviderResponse findById(Long id) {
        return ProviderResponse.fromEntity(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Provider findEntityById(Long id) {
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        return providerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> listAll(String name, String orderBy) {
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        return providerRepository.findByUserId(userId).stream()
                .filter(provider -> matchesName(provider, name))
                .sorted(createSort(orderBy))
                .map(ProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProviderResponse save(ProviderRequest request) {
        User currentUser = authenticatedUserService.getAuthenticatedUser();
        Provider provider = new Provider();
        provider.setUser(currentUser);
        applyRequest(provider, request);
        return ProviderResponse.fromEntity(providerRepository.save(provider));
    }

    @Transactional
    public ProviderResponse update(Long id, ProviderRequest request) {
        Provider existingProvider = findEntityById(id);
        applyRequest(existingProvider, request);
        return ProviderResponse.fromEntity(providerRepository.save(existingProvider));
    }

    @Transactional
    public void delete(Long id) {
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        Provider provider = findEntityById(id);

        if (productRepository.existsByProviderIdAndUserId(id, userId)) {
            throw new BusinessException("Cannot delete provider as it is currently associated with existing products.");
        }

        providerRepository.delete(provider);
    }

    @Transactional
    public ProviderResponse copy(Long id) {
        Provider existingProvider = findEntityById(id);
        String baseName = COPY_SUFFIX_PATTERN.matcher(existingProvider.getName()).replaceFirst("");
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        int nextCopyNumber = findNextCopyNumber(baseName, userId);

        Provider copiedProvider = new Provider();
        copiedProvider.setUser(authenticatedUserService.getAuthenticatedUser());
        copiedProvider.setName(String.format("%s - Copy (%d)", baseName, nextCopyNumber));
        copiedProvider.setCnpj(existingProvider.getCnpj());
        copiedProvider.setNotes(existingProvider.getNotes());
        copiedProvider.setPhone(existingProvider.getPhone());
        copiedProvider.setEmail(existingProvider.getEmail());
        copiedProvider.setAddress(existingProvider.getAddress());
        return ProviderResponse.fromEntity(providerRepository.save(copiedProvider));
    }

    private void applyRequest(Provider provider, ProviderRequest request) {
        provider.setName(request.name().trim());
        provider.setCnpj(request.cnpj());
        provider.setNotes(request.notes());
        provider.setPhone(request.phone());
        provider.setEmail(request.email());
        provider.setAddress(request.address());
    }

    private boolean matchesName(Provider provider, String name) {
        if (name == null || name.isBlank()) {
            return true;
        }

        return provider.getName() != null
                && provider.getName().toLowerCase(Locale.ROOT).contains(name.trim().toLowerCase(Locale.ROOT));
    }

    private Comparator<Provider> createSort(String orderBy) {
        Comparator<Provider> nameComparator = Comparator.comparing(
                provider -> Objects.toString(provider.getName(), ""),
                String.CASE_INSENSITIVE_ORDER);

        if ("name_desc".equalsIgnoreCase(orderBy)) {
            return nameComparator.reversed();
        }

        return nameComparator;
    }

    private int findNextCopyNumber(String baseName, Long userId) {
        return providerRepository.findByUserId(userId).stream()
                .map(Provider::getName)
                .filter(Objects::nonNull)
                .filter(name -> name.equals(baseName) || name.startsWith(baseName + " - Copy ("))
                .mapToInt(this::extractCopyNumber)
                .max()
                .orElse(0) + 1;
    }

    private int extractCopyNumber(String name) {
        Matcher matcher = COPY_SUFFIX_PATTERN.matcher(name);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 0;
    }
}
