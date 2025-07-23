package com.flick.business.api.dto.response.common;

import java.math.BigDecimal;

public record ChartDataPoint(
        String label,
        BigDecimal value) {
}