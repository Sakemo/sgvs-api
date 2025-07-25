package com.flick.business.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.request.settings.GeneralSettingsRequest;
import com.flick.business.api.dto.response.GeneralSettingsResponse;
import com.flick.business.api.mapper.GeneralSettingsMapper;
import com.flick.business.core.entity.GeneralSettings;
import com.flick.business.repository.GeneralSettingsRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeneralSettingsService {
    private final GeneralSettingsRepository settingsRepository;
    private final GeneralSettingsMapper settingsMapper;
    private static final Long SETTINGS_ID = 1L;

    @PostConstruct
    @Transactional
    public void init() {
        if (!settingsRepository.existsById(SETTINGS_ID)) {
            GeneralSettings defaultSettings = new GeneralSettings();
            defaultSettings.setId(SETTINGS_ID);
            settingsRepository.save(defaultSettings);
        }
    }

    @Transactional(readOnly = true)
    public GeneralSettingsResponse getSettings() {
        return GeneralSettingsResponse.fromEntity(findEntity());
    }

    /**
     * Search for the settings entity
     * 
     * @return GeneralSettings entity
     */
    @Transactional(readOnly = true)
    public GeneralSettings findEntity() {
        return settingsRepository.findById(SETTINGS_ID).get();
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
