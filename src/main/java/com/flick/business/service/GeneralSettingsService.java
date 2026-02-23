package com.flick.business.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.service.security.AuthenticatedUserService;
import com.flick.business.api.dto.request.settings.GeneralSettingsRequest;
import com.flick.business.api.dto.response.GeneralSettingsResponse;
import com.flick.business.api.mapper.GeneralSettingsMapper;
import com.flick.business.core.entity.GeneralSettings;
import com.flick.business.core.enums.settings.StockControlType;
import com.flick.business.repository.GeneralSettingsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeneralSettingsService {
    private final AuthenticatedUserService authenticatedUserService;
    private final GeneralSettingsRepository settingsRepository;
    private final GeneralSettingsMapper settingsMapper;

    @Transactional
    public GeneralSettingsResponse getSettings() {
        return GeneralSettingsResponse.fromEntity(findEntity());
    }

    /**
     * Search for the settings entity
     *
     * @return GeneralSettings entity
     */
    @Transactional
    public GeneralSettings findEntity() {
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    GeneralSettings newSettings = new GeneralSettings();
                    newSettings.setUser(authenticatedUserService.getAuthenticatedUser());
                    newSettings.setStockControlType(StockControlType.PER_ITEM);
                    return settingsRepository.save(newSettings);
                });
    }

    /**
     * Save or update settings
     *
     * @param request - entity to be updated
     * @return GeneralSettingsRequest updated
     */
    @Transactional
    public GeneralSettingsResponse update(GeneralSettingsRequest request) {
        GeneralSettings existingSettings = findEntity();
        settingsMapper.updateEntityFromRequest(request, existingSettings);
        GeneralSettings updatedSettings = settingsRepository.save(existingSettings);
        return GeneralSettingsResponse.fromEntity(updatedSettings);
    }
}
