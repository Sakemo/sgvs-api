package com.flick.business.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flick.business.api.dto.request.GeneralSettingsRequest;
import com.flick.business.api.dto.response.GeneralSettingsResponse;
import com.flick.business.service.GeneralSettingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class GeneralSettingsController {
    private final GeneralSettingsService settingsService;

    @GetMapping
    public ResponseEntity<GeneralSettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<GeneralSettingsResponse> updateSettings(@Valid @RequestBody GeneralSettingsRequest request) {
        return ResponseEntity.ok(settingsService.update(request));
    }
}
