package com.flick.business.api.dto.response.common;

import java.math.BigDecimal;
import java.util.List;

public record MetricCardData(
        BigDecimal value,
        BigDecimal percentageChange,
        List<BigDecimal> sparklineData) {
}