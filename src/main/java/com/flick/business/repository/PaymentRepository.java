package com.flick.business.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flick.business.core.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p " +
            "WHERE p.paymentDate < :beforeDate " +
            "AND p.customer.user.id = :userId")
    BigDecimal sumAmountPaidBeforeDate(
            @Param("beforeDate") ZonedDateTime beforeDate,
            @Param("userId") Long userId);

    @Query("SELECT CAST(p.paymentDate AS date), COALESCE(SUM(p.amountPaid), 0) FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.customer.user.id = :userId " +
            "GROUP BY CAST(p.paymentDate AS date) ORDER BY CAST(p.paymentDate AS date)")
    List<Object[]> sumAmountPaidByDayBetween(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate,
            @Param("userId") Long userId);
}
