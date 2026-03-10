package com.flick.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.response.aministration.reports.AbcAnalysisResponse;
import com.flick.business.api.dto.response.reports.FinancialSummaryResponse;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.SaleItemRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.service.security.AuthenticatedUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    private static final Set<ExpenseType> OPERATING_EXPENSE_TYPES = EnumSet.of(
            ExpenseType.BUSINESS,
            ExpenseType.OTHERS);

    private static final Set<ExpenseType> NON_OPERATING_EXPENSE_TYPES = EnumSet.of(
            ExpenseType.PERSONAL,
            ExpenseType.INVESTMENT);

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ExpenseRepository expenseRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary(ZonedDateTime startDate, ZonedDateTime endDate) {
        // search expenses
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        BigDecimal totalRevenue = saleRepository.sumTotalValueBetweenDates(startDate, endDate, userId);
        BigDecimal totalCogs = saleRepository.sumTotalCostOfGoodsSoldBetween(startDate, endDate, userId);
        BigDecimal totalOperatingExpenses = expenseRepository.sumTotalValueBetweenDatesByTypes(
                startDate, endDate, userId, OPERATING_EXPENSE_TYPES);
        BigDecimal totalNonOperatingExpenses = expenseRepository.sumTotalValueBetweenDatesByTypes(
                startDate, endDate, userId, NON_OPERATING_EXPENSE_TYPES);
        BigDecimal totalExpenses = totalOperatingExpenses.add(totalNonOperatingExpenses);

        // calculate profits
        BigDecimal grossProfit = totalRevenue.subtract(totalCogs);
        BigDecimal operatingProfit = grossProfit.subtract(totalOperatingExpenses);
        BigDecimal netProfit = operatingProfit.subtract(totalNonOperatingExpenses);

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
        Long userId = authenticatedUserService.getAuthenticatedUserId();
        List<Object[]> rawResults = saleItemRepository.performAbcAnalysis(startDate, endDate, userId);

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
