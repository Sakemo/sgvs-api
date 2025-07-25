package com.flick.business.api.dto.response.aministration;

import java.util.List;

import com.flick.business.api.dto.response.common.ChartDataPoint;
import com.flick.business.api.dto.response.common.MetricCardData;
import com.flick.business.api.dto.response.common.TimeSeriesDataPoint;

public record DashboardResponse(
        MetricCardData grossRevenue,
        MetricCardData netProfit,
        MetricCardData totalExpense,
        MetricCardData totalReceivables,
        MetricCardData averageTicket,
        List<ChartDataPoint> salesByPaymentMethod,
        List<ChartDataPoint> topSellingProducts,
        List<TimeSeriesDataPoint> revenueAndProfitTrend) {
}