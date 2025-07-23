package com.flick.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.response.DashboardResponse;
import com.flick.business.api.dto.response.common.ChartDataPoint;
import com.flick.business.api.dto.response.common.MetricCardData;
import com.flick.business.api.dto.response.common.TimeSeriesDataPoint;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.SaleItemRepository;
import com.flick.business.repository.SaleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final SaleItemRepository saleItemRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary(ZonedDateTime startDate, ZonedDateTime endDate) {
        long durationDays = Duration.between(startDate, endDate).toDays() + 1;
        ZonedDateTime previousStartDate = startDate.minusDays(durationDays);
        ZonedDateTime previousEndDate = endDate.minusDays(durationDays);

        BigDecimal currentGrossRevenue = saleRepository.sumTotalValueBetweenDates(startDate, endDate);
        BigDecimal previousGrossRevenue = saleRepository.sumTotalValueBetweenDates(previousStartDate, previousEndDate);

        BigDecimal currentTotalExpenses = expenseRepository.sumTotalValueBetweenDates(startDate, endDate);
        BigDecimal previousTotalExpenses = expenseRepository.sumTotalValueBetweenDates(previousStartDate,
                previousEndDate);

        List<Object[]> salesByPaymentMethodRaw = saleRepository.sumTotalGroupByPaymentMethodBetween(startDate, endDate);
        List<Object[]> topSellingProductsRaw = saleItemRepository.findTop5SellingProductsByRevenue(startDate, endDate);
        List<Object[]> revenueTrendRaw = saleRepository.findRevenueByDay(startDate, endDate);

        MetricCardData grossRevenueCard = buildMetricCard(currentGrossRevenue, previousGrossRevenue);
        MetricCardData totalExpensesCard = buildMetricCard(currentTotalExpenses, previousTotalExpenses);

        BigDecimal currentNetProfit = currentGrossRevenue.subtract(currentTotalExpenses);
        BigDecimal previousNetProfit = previousGrossRevenue.subtract(previousTotalExpenses);
        MetricCardData netProfitCard = buildMetricCard(currentNetProfit, previousNetProfit);

        List<ChartDataPoint> salesByPaymentMethod = salesByPaymentMethodRaw.stream()
                .map(row -> new ChartDataPoint(((PaymentMethod) row[0]).toString(), (BigDecimal) row[1]))
                .collect(Collectors.toList());

        List<ChartDataPoint> topSellingProducts = topSellingProductsRaw.stream()
                .map(row -> new ChartDataPoint((String) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());

        List<TimeSeriesDataPoint> trend = revenueTrendRaw.stream()
                .map(row -> new TimeSeriesDataPoint(row[0].toString(), (BigDecimal) row[1], BigDecimal.ZERO))
                .collect(Collectors.toList());

        return new DashboardResponse(
                grossRevenueCard,
                netProfitCard,
                totalExpensesCard,
                salesByPaymentMethod,
                topSellingProducts,
                trend);
    }

    private MetricCardData buildMetricCard(BigDecimal currentValue, BigDecimal previousValue) {
        BigDecimal percentageChange = calculatePercentageChange(currentValue, previousValue);
        List<BigDecimal> sparklineData = Collections.emptyList();

        return new MetricCardData(currentValue, percentageChange, sparklineData);
    }

    private BigDecimal calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return (current != null && current.compareTo(BigDecimal.ZERO) > 0) ? new BigDecimal("100.0")
                    : BigDecimal.ZERO;
        }
        if (current == null) {
            return new BigDecimal("-100.0");
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
