package com.flick.business.api.dto.response.reports;

import java.math.BigDecimal;

public record FinancialSummaryResponse(
                BigDecimal totalRevenue,
                BigDecimal totalCostOfGoods,
                BigDecimal totalExpenses,

                BigDecimal grossProfit,
                BigDecimal netProfit,
                BigDecimal operatingProfit,

                BigDecimal grossMargin,
                BigDecimal netMargin,
                BigDecimal operatingMargin) {
}