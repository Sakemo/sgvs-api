package com.flick.business.repository;

import com.flick.business.core.entity.Expense;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.core.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
  Optional<Expense> findByIdAndUserId(Long id, Long userId);

  /**
   * Sums the total value of expenses for a user within a specified date range.
   *
   * @param startDate The start date of the range.
   * @param endDate   The end date of the range.
   * @param userId    The ID of the user.
   * @return The total value of expenses within the date range for the user.
   */
  @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e " +
      "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
      "AND e.user.id = :userId")
  BigDecimal sumTotalValueBetweenDates(
      @Param("startDate") ZonedDateTime startDate,
      @Param("endDate") ZonedDateTime endDate,
      @Param("userId") Long userId);

  @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e " +
      "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
      "AND e.user.id = :userId " +
      "AND e.paymentMethod = :paymentMethod")
  BigDecimal sumTotalValueBetweenDatesAndPaymentMethod(
      @Param("startDate") ZonedDateTime startDate,
      @Param("endDate") ZonedDateTime endDate,
      @Param("userId") Long userId,
      @Param("paymentMethod") PaymentMethod paymentMethod);

  @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e " +
      "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
      "AND e.user.id = :userId " +
      "AND e.paymentMethod = :paymentMethod " +
      "AND e.paid = false")
  BigDecimal sumTotalUnpaidByPaymentMethodBetweenDates(
      @Param("startDate") ZonedDateTime startDate,
      @Param("endDate") ZonedDateTime endDate,
      @Param("userId") Long userId,
      @Param("paymentMethod") PaymentMethod paymentMethod);

  @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e " +
      "WHERE e.user.id = :userId " +
      "AND e.paymentMethod = :paymentMethod " +
      "AND e.paid = false")
  BigDecimal sumTotalUnpaidByPaymentMethod(
      @Param("userId") Long userId,
      @Param("paymentMethod") PaymentMethod paymentMethod);

  @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e " +
      "WHERE e.expenseDate <= :endDate " +
      "AND e.user.id = :userId " +
      "AND e.paymentMethod = :paymentMethod " +
      "AND e.paid = false")
  BigDecimal sumTotalUnpaidByPaymentMethodUpToDate(
      @Param("endDate") ZonedDateTime endDate,
      @Param("userId") Long userId,
      @Param("paymentMethod") PaymentMethod paymentMethod);

  @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e " +
      "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
      "AND e.user.id = :userId " +
      "AND e.expenseType IN :types")
  BigDecimal sumTotalValueBetweenDatesByTypes(
      @Param("startDate") ZonedDateTime startDate,
      @Param("endDate") ZonedDateTime endDate,
      @Param("userId") Long userId,
      @Param("types") Set<ExpenseType> types);

  @Query("SELECT CAST(e.expenseDate AS date), SUM(e.value) FROM Expense e " +
      "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
      "AND e.user.id = :userId " +
      "GROUP BY CAST(e.expenseDate AS date) ORDER BY CAST(e.expenseDate AS date)")
  List<Object[]> findExpenseByDay(@Param("startDate") ZonedDateTime startDate,
      @Param("endDate") ZonedDateTime endDate,
      @Param("userId") Long userId);

  @Query("SELECT CAST(e.expenseDate AS date), COALESCE(SUM(e.value), 0) FROM Expense e " +
      "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
      "AND e.user.id = :userId " +
      "AND e.expenseType = :expenseType " +
      "GROUP BY CAST(e.expenseDate AS date) ORDER BY CAST(e.expenseDate AS date)")
  List<Object[]> sumTotalGroupByDayByTypeBetween(
      @Param("startDate") ZonedDateTime startDate,
      @Param("endDate") ZonedDateTime endDate,
      @Param("userId") Long userId,
      @Param("expenseType") ExpenseType expenseType);
}
