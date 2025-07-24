package com.flick.business.api.dto.response.common;

import java.math.BigDecimal;

public record TimeSeriesDataPoint(
                String date,
                BigDecimal revenue,
                BigDecimal profit,
                BigDecimal receivables) {
}