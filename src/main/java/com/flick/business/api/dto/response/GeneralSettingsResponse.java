package com.flick.business.api.dto.response;

import com.flick.business.core.entity.GeneralSettings;
import com.flick.business.core.enums.settings.StockControlType;

public record GeneralSettingsResponse(
        Long id,
        StockControlType stockControlType,
        String businessName,
        String businessField) {
    public static GeneralSettingsResponse fromEntity(GeneralSettings entity) {
        return new GeneralSettingsResponse(
                entity.getId(),
                entity.getStockControlType(),
                entity.getBusinessName(),
                entity.getBusinessField());
    }
}
