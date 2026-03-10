package com.flick.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flick.business.api.dto.response.aministration.reports.AbcAnalysisResponse;
import com.flick.business.api.dto.response.reports.FinancialSummaryResponse;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.SaleItemRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.service.security.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Report Service Tests")
class ReportServiceTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private SaleItemRepository saleItemRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("should calculate financial summary with distinct operating and net profits")
    void getFinancialSummary_calculatesExpectedMetrics() {
        Long userId = 1L;
        ZonedDateTime startDate = ZonedDateTime.parse("2026-01-01T00:00:00Z");
        ZonedDateTime endDate = ZonedDateTime.parse("2026-01-31T23:59:59Z");

        when(authenticatedUserService.getAuthenticatedUserId()).thenReturn(userId);
        when(saleRepository.sumTotalValueBetweenDates(startDate, endDate, userId))
                .thenReturn(new BigDecimal("1000.00"));
        when(saleRepository.sumTotalCostOfGoodsSoldBetween(startDate, endDate, userId))
                .thenReturn(new BigDecimal("400.00"));
        when(expenseRepository.sumTotalValueBetweenDatesByTypes(eq(startDate), eq(endDate), eq(userId), anySet()))
                .thenReturn(new BigDecimal("150.00"), new BigDecimal("50.00"));

        FinancialSummaryResponse result = reportService.getFinancialSummary(startDate, endDate);

        assertThat(result.totalRevenue()).isEqualByComparingTo("1000.00");
        assertThat(result.totalCostOfGoods()).isEqualByComparingTo("400.00");
        assertThat(result.totalExpenses()).isEqualByComparingTo("200.00");
        assertThat(result.grossProfit()).isEqualByComparingTo("600.00");
        assertThat(result.operatingProfit()).isEqualByComparingTo("450.00");
        assertThat(result.netProfit()).isEqualByComparingTo("400.00");
        assertThat(result.grossMargin()).isEqualByComparingTo("60.0000");
        assertThat(result.operatingMargin()).isEqualByComparingTo("45.0000");
        assertThat(result.netMargin()).isEqualByComparingTo("40.0000");
    }

    @Test
    @DisplayName("should return zero margins when total revenue is zero")
    void getFinancialSummary_whenRevenueIsZero_returnsZeroMargins() {
        Long userId = 1L;
        ZonedDateTime startDate = ZonedDateTime.parse("2026-02-01T00:00:00Z");
        ZonedDateTime endDate = ZonedDateTime.parse("2026-02-28T23:59:59Z");

        when(authenticatedUserService.getAuthenticatedUserId()).thenReturn(userId);
        when(saleRepository.sumTotalValueBetweenDates(startDate, endDate, userId)).thenReturn(BigDecimal.ZERO);
        when(saleRepository.sumTotalCostOfGoodsSoldBetween(startDate, endDate, userId))
                .thenReturn(new BigDecimal("20.00"));
        when(expenseRepository.sumTotalValueBetweenDatesByTypes(eq(startDate), eq(endDate), eq(userId), anySet()))
                .thenReturn(new BigDecimal("30.00"), new BigDecimal("10.00"));

        FinancialSummaryResponse result = reportService.getFinancialSummary(startDate, endDate);

        assertThat(result.grossMargin()).isEqualByComparingTo("0");
        assertThat(result.operatingMargin()).isEqualByComparingTo("0");
        assertThat(result.netMargin()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("should map ABC analysis query rows correctly")
    void getAbcAnalisys_mapsRows() {
        Long userId = 1L;
        ZonedDateTime startDate = ZonedDateTime.parse("2026-03-01T00:00:00Z");
        ZonedDateTime endDate = ZonedDateTime.parse("2026-03-31T23:59:59Z");

        when(authenticatedUserService.getAuthenticatedUserId()).thenReturn(userId);
        when(saleItemRepository.performAbcAnalysis(startDate, endDate, userId))
                .thenReturn(List.<Object[]>of(
                        new Object[] { 10L, "Produto A", new BigDecimal("500.00"), 50.0, 50.0, "A" }));

        List<AbcAnalysisResponse> result = reportService.getAbcAnalisys(startDate, endDate);

        assertThat(result).hasSize(1);
        AbcAnalysisResponse row = result.get(0);
        assertThat(row.productId()).isEqualTo(10L);
        assertThat(row.productName()).isEqualTo("Produto A");
        assertThat(row.totalRevenue()).isEqualByComparingTo("500.00");
        assertThat(row.percentageOfTotalRevenue()).isEqualTo(50.0);
        assertThat(row.cumulativePercentage()).isEqualTo(50.0);
        assertThat(row.abcClass()).isEqualTo("A");
    }
}
