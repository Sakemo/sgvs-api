package com.flick.business.api.dto.response;

import java.util.List;

import com.flick.business.api.dto.response.common.ChartDataPoint;
import com.flick.business.api.dto.response.common.MetricCardData;
import com.flick.business.api.dto.response.common.TimeSeriesDataPoint;

public record DashboardResponse(
                MetricCardData grossRevenue,
                MetricCardData netProfit,
                MetricCardData totalExpense,
                MetricCardData newCustomers,
                MetricCardData averageTicket,
                List<ChartDataPoint> salesByPaymentMethod,
                List<ChartDataPoint> topSellingProducts,
                List<TimeSeriesDataPoint> revenueAndProfitTrend) {
}