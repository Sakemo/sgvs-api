package com.flick.business.repository;

import com.flick.business.core.entity.Sale;
import com.flick.business.core.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    /**
     * Calculates the gross total of sales based on a set of filters.
     */
    @Query("SELECT COALESCE(SUM(s.totalValue), 0) FROM Sale s " +
            "WHERE (:startDate IS NULL OR s.saleDate >= :startDate) " +
            "AND (:endDate IS NULL OR s.saleDate <= :endDate) " +
            "AND (:customerId IS NULL OR s.customer.id = :customerId) " +
            "AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod) " +
            "AND (:productId IS NULL OR EXISTS (SELECT 1 FROM SaleItem si WHERE si.sale = s AND si.product.id = :productId))")
    BigDecimal getGrossTotalWithFilters(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate,
            @Param("customerId") Long customerId,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("productId") Long productId);

    /**
     * Groups sales by payment method and calculates the total for each, within a
     * date range.
     */
    @Query("SELECT s.paymentMethod, SUM(s.totalValue) FROM Sale s " +
            "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
            "GROUP BY s.paymentMethod")
    List<Object[]> sumTotalGroupByPaymentMethodBetween(
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

}