package com.flick.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.response.aministration.reports.AbcAnalysisResponse;
import com.flick.business.api.dto.response.reports.FinancialSummaryResponse;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.SaleItemRepository;
import com.flick.business.repository.SaleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary(ZonedDateTime startDate, ZonedDateTime endDate) {
        // search expenses
        BigDecimal totalRevenue = saleRepository.sumTotalValueBetweenDates(startDate, endDate);
        BigDecimal totalCogs = saleRepository.sumTotalCostOfGoodsSoldBetween(startDate, endDate);
        BigDecimal totalExpenses = expenseRepository.sumTotalValueBetweenDates(startDate, endDate);

        // calculate profits
        BigDecimal grossProfit = totalRevenue.subtract(totalCogs);
        BigDecimal netProfit = grossProfit.subtract(totalExpenses);
        BigDecimal operatingProfit = grossProfit.subtract(totalExpenses);

        // calculate margins
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal hundred = new BigDecimal("100");

        BigDecimal grossMargin = (totalRevenue.compareTo(zero) > 0)
                ? grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(hundred)
                : zero;

        BigDecimal netMargin = (totalRevenue.compareTo(zero) > 0)
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(hundred)
                : zero;

        BigDecimal operatingMargin = (totalRevenue.compareTo(zero) > 0)
                ? operatingProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(hundred)
                : zero;

        return new FinancialSummaryResponse(
                totalRevenue, totalCogs, totalExpenses,
                grossProfit, netProfit, operatingProfit,
                grossMargin, netMargin, operatingMargin);
    }

    @Transactional(readOnly = true)
    public List<AbcAnalysisResponse> getAbcAnalisys(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<Object[]> rawResults = saleItemRepository.performAbcAnalysis(startDate, endDate);

        return rawResults.stream()
                .map(row -> new AbcAnalysisResponse(
                        ((Number) row[0]).longValue(), // productId
                        (String) row[1], // productName
                        (BigDecimal) row[2], // totalRevenue
                        ((Number) row[3]).doubleValue(), // percentageOfTotalRevenue
                        ((Number) row[4]).doubleValue(), // cumulativePercentage
                        (String) row[5] // abcClass
                ))
                .collect(Collectors.toList());
    }
}
