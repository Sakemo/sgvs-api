package com.flick.business.api.dto.request.settings;

import com.flick.business.core.enums.settings.StockControlType;

import jakarta.validation.constraints.NotNull;

public record GeneralSettingsRequest(
                @NotNull StockControlType stockControlType,

                String businessName,
                String businessField) {
}
