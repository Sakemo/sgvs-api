package com.flick.business.api.dto.response.aministration.reports;

import java.math.BigDecimal;

public record AbcAnalysisResponse(
                Long productId,
                String productName,
                BigDecimal totalRevenue,
                double percentageOfTotalRevenue,
                double cumulativePercentage,
                String abcClass) {
}