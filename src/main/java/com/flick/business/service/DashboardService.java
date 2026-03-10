package com.flick.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.response.aministration.DashboardResponse;
import com.flick.business.api.dto.response.common.ChartDataPoint;
import com.flick.business.api.dto.response.common.MetricCardData;
import com.flick.business.api.dto.response.common.TimeSeriesDataPoint;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.PaymentRepository;
import com.flick.business.repository.SaleItemRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.repository.CustomerRepository;
import com.flick.business.service.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
        private static final Set<ExpenseType> DASHBOARD_EXPENSE_TYPES = EnumSet.of(
                        ExpenseType.BUSINESS,
                        ExpenseType.PERSONAL,
                        ExpenseType.INVESTMENT,
                        ExpenseType.OTHERS);

        private final SaleRepository saleRepository;
        private final ExpenseRepository expenseRepository;
        private final PaymentRepository paymentRepository;
        private final SaleItemRepository saleItemRepository;
        private final CustomerRepository customerRepository;
        private final AuthenticatedUserService authenticatedUserService;

        @Transactional(readOnly = true)
        public DashboardResponse getDashboardSummary(ZonedDateTime startDate, ZonedDateTime endDate) {
                Long userId = authenticatedUserService.getAuthenticatedUserId();
                long durationDays = Duration.between(startDate, endDate).toDays() + 1;
                ZonedDateTime previousStartDate = startDate.minusDays(durationDays);
                ZonedDateTime previousEndDate = endDate.minusDays(durationDays);

                Long currentSaleCount = saleRepository.countSalesBetween(startDate, endDate, userId);
                Long previousSaleCount = saleRepository.countSalesBetween(previousStartDate, previousEndDate, userId);

                BigDecimal currentGrossRevenue = saleRepository.sumTotalValueBetweenDates(startDate, endDate, userId);
                BigDecimal previousGrossRevenue = saleRepository.sumTotalValueBetweenDates(previousStartDate,
                                previousEndDate, userId);

                BigDecimal currentTotalExpenses = expenseRepository.sumTotalValueBetweenDatesByTypes(
                                startDate, endDate, userId, DASHBOARD_EXPENSE_TYPES);
                BigDecimal previousTotalExpenses = expenseRepository.sumTotalValueBetweenDatesByTypes(
                                previousStartDate, previousEndDate, userId, DASHBOARD_EXPENSE_TYPES);

                List<Object[]> salesByPaymentMethodRaw = saleRepository.sumTotalGroupByPaymentMethodBetween(startDate,
                                endDate, userId);
                List<Object[]> topSellingProductsRaw = saleItemRepository.findTop10SellingProductsByRevenue(startDate,
                                endDate, userId);
                List<Object[]> revenueTrendRaw = saleRepository.findRevenueByDay(startDate, endDate, userId);
                List<Object[]> expenseTrendBusinessRaw = expenseRepository.sumTotalGroupByDayByTypeBetween(startDate,
                                endDate, userId, ExpenseType.BUSINESS);
                List<Object[]> expenseTrendPersonalRaw = expenseRepository.sumTotalGroupByDayByTypeBetween(startDate,
                                endDate, userId, ExpenseType.PERSONAL);
                List<Object[]> expenseTrendInvestmentRaw = expenseRepository.sumTotalGroupByDayByTypeBetween(startDate,
                                endDate, userId, ExpenseType.INVESTMENT);
                List<Object[]> expenseTrendOthersRaw = expenseRepository.sumTotalGroupByDayByTypeBetween(startDate,
                                endDate, userId, ExpenseType.OTHERS);
                List<Object[]> creditSalesTrendRaw = saleRepository.sumTotalGroupByDayAndPaymentMethodBetween(startDate,
                                endDate, userId, PaymentMethod.ON_CREDIT);
                List<Object[]> paymentsTrendRaw = paymentRepository.sumAmountPaidByDayBetween(startDate, endDate, userId);

                BigDecimal currentAverageTicket = (currentSaleCount > 0)
                                ? currentGrossRevenue.divide(new BigDecimal(currentSaleCount), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                BigDecimal previousAverageTicket = (previousSaleCount > 0)
                                ? previousGrossRevenue.divide(new BigDecimal(previousSaleCount), 2,
                                                RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                BigDecimal currentTotalReceivables = customerRepository.findTotalDebtBalance(userId);
                BigDecimal previousTotalReceivables = currentTotalReceivables;

                MetricCardData totalReceivablesCard = buildMetricCard(currentTotalReceivables,
                                previousTotalReceivables);
                MetricCardData averageTicketCard = buildMetricCard(currentAverageTicket, previousAverageTicket);

                MetricCardData grossRevenueCard = buildMetricCard(currentGrossRevenue, previousGrossRevenue);
                MetricCardData totalExpensesCard = buildMetricCard(currentTotalExpenses, previousTotalExpenses);

                BigDecimal currentNetProfit = currentGrossRevenue.subtract(currentTotalExpenses);
                BigDecimal previousNetProfit = previousGrossRevenue.subtract(previousTotalExpenses);
                MetricCardData netProfitCard = buildMetricCard(currentNetProfit, previousNetProfit);

                Map<String, BigDecimal> revenueByDate = revenueTrendRaw.stream()
                                .collect(Collectors.toMap(
                                                row -> row[0].toString(),
                                                row -> (BigDecimal) row[1]));

                Map<String, BigDecimal> expenseByDate = new HashMap<>();

                Stream.of(expenseTrendBusinessRaw, expenseTrendPersonalRaw, expenseTrendInvestmentRaw, expenseTrendOthersRaw)
                                .forEach(raw -> raw.forEach(row -> expenseByDate.merge(
                                                row[0].toString(),
                                                (BigDecimal) row[1],
                                                BigDecimal::add)));

                Map<String, BigDecimal> creditSalesByDate = creditSalesTrendRaw.stream()
                                .collect(Collectors.toMap(
                                                row -> row[0].toString(),
                                                row -> (BigDecimal) row[1]));

                Map<String, BigDecimal> paymentsByDate = paymentsTrendRaw.stream()
                                .collect(Collectors.toMap(
                                                row -> row[0].toString(),
                                                row -> (BigDecimal) row[1]));

                List<ChartDataPoint> salesByPaymentMethod = salesByPaymentMethodRaw.stream()
                                .map(row -> new ChartDataPoint(((PaymentMethod) row[0]).toString(),
                                                (BigDecimal) row[1]))
                                .collect(Collectors.toList());

                List<String> trendDates = Stream
                                .of(
                                                revenueByDate.keySet().stream(),
                                                expenseByDate.keySet().stream(),
                                                creditSalesByDate.keySet().stream(),
                                                paymentsByDate.keySet().stream())
                                .flatMap(stream -> stream)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList());

                BigDecimal receivablesRunning = saleRepository
                                .sumTotalValueBeforeDateByPaymentMethod(startDate, userId, PaymentMethod.ON_CREDIT)
                                .subtract(paymentRepository.sumAmountPaidBeforeDate(startDate, userId));

                List<TimeSeriesDataPoint> trend = new ArrayList<>();
                for (String date : trendDates) {
                        BigDecimal revenue = revenueByDate.getOrDefault(date, BigDecimal.ZERO);
                        BigDecimal expense = expenseByDate.getOrDefault(date, BigDecimal.ZERO);
                        BigDecimal profit = revenue.subtract(expense);
                        BigDecimal creditSales = creditSalesByDate.getOrDefault(date, BigDecimal.ZERO);
                        BigDecimal payments = paymentsByDate.getOrDefault(date, BigDecimal.ZERO);

                        receivablesRunning = receivablesRunning.add(creditSales).subtract(payments);
                        BigDecimal receivablesAtDate = receivablesRunning.max(BigDecimal.ZERO);

                        trend.add(new TimeSeriesDataPoint(date, revenue, profit, receivablesAtDate));
                }

                List<ChartDataPoint> topSellingProducts = topSellingProductsRaw.stream()
                                .map(row -> new ChartDataPoint((String) row[0], (BigDecimal) row[1]))
                                .collect(Collectors.toList());

                return new DashboardResponse(
                                grossRevenueCard,
                                netProfitCard,
                                totalExpensesCard,
                                totalReceivablesCard,
                                averageTicketCard,
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
