package com.flick.business.repository;

import com.flick.business.core.entity.Sale;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.core.enums.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {
        Optional<Sale> findByIdAndUserId(Long id, Long userId);

        /**
         * Calculates the gross total of sales based on a set of filters.
         */
        @Query("SELECT COALESCE(SUM(s.totalValue), 0) FROM Sale s " +
                        "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
                        "AND s.user.id = :userId " +
                        "AND (:customerId IS NULL OR s.customer.id = :customerId) " +
                        "AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod) " +
                        "AND (:paymentStatus IS NULL OR s.paymentStatus = :paymentStatus) " +
                        "AND (:productId IS NULL OR EXISTS (SELECT 1 FROM SaleItem si WHERE si.sale = s AND si.product.id = :productId))")
        BigDecimal getGrossTotalWithFilters(
                        @Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId,
                        @Param("customerId") Long customerId,
                        @Param("paymentMethod") PaymentMethod paymentMethod,
                        @Param("paymentStatus") PaymentStatus paymentStatus,
                        @Param("productId") Long productId);

        /**
         * Groups sales by payment method and calculates the total for each, within a
         * date range.
         */
        @Query("SELECT s.paymentMethod, SUM(s.totalValue) FROM Sale s " +
                        "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
                        "AND s.user.id = :userId " +
                        "GROUP BY s.paymentMethod")
        List<Object[]> sumTotalGroupByPaymentMethodBetween(
                        @Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);;

        @Query("SELECT FUNCTION('TO_CHAR', s.saleDate, 'YYYY-MM-DD'), SUM(s.totalValue) " +
                        "FROM Sale s WHERE " +
                        "(:startDate IS NULL OR s.saleDate >= :startDate) AND (:endDate IS NULL OR s.saleDate <= :endDate) " +
                        "AND s.user.id = :userId "
                        +
                        "GROUP BY FUNCTION('TO_CHAR', s.saleDate, 'YYYY-MM-DD')")
        List<Object[]> sumTotalGroupByDay(@Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);

        @Query("SELECT s.customer.id, s.customer.name, SUM(s.totalValue) " +
                        "FROM Sale s WHERE " +
                        "(:startDate IS NULL OR s.saleDate >= :startDate) AND (:endDate IS NULL OR s.saleDate <= :endDate) " +
                        "AND s.user.id = :userId "
                        +
                        "GROUP BY s.customer.id, s.customer.name")
        List<Object[]> sumTotalGroupByCustomer(@Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);

        @Query("SELECT s.customer.id FROM Sale s WHERE s.customer IS NOT NULL AND s.user.id = :userId GROUP BY s.customer.id ORDER BY COUNT(s.id) DESC LIMIT 3")
        List<Long> findTop3MostFrequentCustomerIds(@Param("userId") Long userId);

        @Query("SELECT COALESCE(SUM(s.totalValue), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.user.id = :userId")
        BigDecimal sumTotalValueBetweenDates(@Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);

        @Query("SELECT CAST(s.saleDate AS date), SUM(s.totalValue) FROM Sale s " +
                        "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
                        "AND s.user.id = :userId " +
                        "GROUP BY CAST(s.saleDate AS date) ORDER BY CAST(s.saleDate AS date)")
        List<Object[]> findRevenueByDay(@Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);

        @Query("SELECT COUNT(s.id) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.user.id = :userId")
        Long countSalesBetween(@Param("startDate") ZonedDateTime startDate, @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);

        @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.user.id = :userId")
        long countByCategoryId(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

        /**
         * Finds all sales for a specific customer that are pending payment.
         * This is used to populate the customer's payment settlement modal.
         *
         * @param customerId    The ID of the customer.
         * @param paymentStatus The status to filter by (typically PENDING).
         * @return A list of pending sales.
         */
        List<Sale> findByCustomerIdAndPaymentStatusAndUserId(Long customerId, PaymentStatus paymentStatus, Long userId);

        /**
         * Calculates the total cost of goods sold (COGS) for all sales within a
         * specific date range.
         * This is done by summing the product of quantity and the product's cost price
         * for each sale item.
         *
         * @param startDate The start of the date range.
         * @param endDate   The end of the date range.
         * @return The total COGS as a BigDecimal, or 0 if no sales are found.
         */
        @Query("SELECT COALESCE(SUM(si.quantity * si.product.costPrice), 0) " +
                        "FROM SaleItem si " +
                        "WHERE si.sale.saleDate BETWEEN :startDate AND :endDate " +
                        "AND si.sale.user.id = :userId " +
                        "AND si.product.costPrice IS NOT NULL")
        BigDecimal sumTotalCostOfGoodsSoldBetween(
                        @Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate,
                        @Param("userId") Long userId);

}
