package com.flick.business.repository;

import com.flick.business.core.entity.Expense;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
        @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "AND e.user.id = :userId")
        BigDecimal sumTotalValueBetweenDates(
                        @Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);

        @Query("SELECT CAST(e.expenseDate AS date), SUM(e.value) FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "AND e.user.id = :userId " +
                        "GROUP BY CAST(e.expenseDate AS date) ORDER BY CAST(e.expenseDate AS date)")
        List<Object[]> findExpenseByDay(@Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);
}
