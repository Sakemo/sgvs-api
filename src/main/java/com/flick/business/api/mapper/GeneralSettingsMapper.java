package com.flick.business.api.mapper;

import org.springframework.stereotype.Component;

import com.flick.business.api.dto.request.GeneralSettingsRequest;
import com.flick.business.core.entity.GeneralSettings;

@Component
public class GeneralSettingsMapper {
    public void updateEntityFromRequest(GeneralSettingsRequest request, GeneralSettings entity) {
        entity.setStockControlType(request.stockControlType());
        entity.setBusinessName(request.businessName());
        entity.setBusinessField(request.businessField());
    }
}
