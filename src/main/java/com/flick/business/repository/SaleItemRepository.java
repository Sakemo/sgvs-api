package com.flick.business.repository;

import com.flick.business.core.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

        @Query("SELECT si.product.id FROM SaleItem si GROUP BY si.product.id ORDER BY SUM(si.quantity) DESC LIMIT 3")
        List<Long> findTop3MostSoldProductIds();

        @Query("SELECT p.name, SUM(si.quantity * si.unitPrice) as totalRevenue " +
                        "FROM SaleItem si JOIN si.product p JOIN si.sale s " +
                        "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY p.name ORDER BY totalRevenue DESC LIMIT 5")
        List<Object[]> findTop5SellingProductsByRevenue(@Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate);

        @Query(value = "WITH ProductRevenue AS ( " +
                        "    SELECT " +
                        "        p.id AS product_id, " +
                        "        p.name AS product_name, " +
                        "        COALESCE(SUM(si.quantity * si.unit_price), 0) AS total_revenue " +
                        "    FROM products p " +
                        "    LEFT JOIN sale_items si ON p.id = si.product_id " +
                        "    LEFT JOIN sales s ON si.sale_id = s.id AND s.sale_date BETWEEN :startDate AND :endDate " +
                        "    WHERE p.active = true " +
                        "    GROUP BY p.id, p.name " +
                        "), " +
                        "TotalRevenue AS ( " +
                        "    SELECT SUM(total_revenue) AS overall_total FROM ProductRevenue " +
                        "), " +
                        "ProductContribution AS ( " +
                        "    SELECT " +
                        "        pr.product_id, " +
                        "        pr.product_name, " +
                        "        pr.total_revenue, " +
                        "        CASE WHEN tr.overall_total > 0 THEN (pr.total_revenue / tr.overall_total) * 100 ELSE 0 END AS percentage_of_total "
                        +
                        "    FROM ProductRevenue pr, TotalRevenue tr " +
                        "    ORDER BY pr.total_revenue DESC " +
                        "), " +
                        "CumulativeContribution AS ( " +
                        "    SELECT " +
                        "        product_id, " +
                        "        product_name, " +
                        "        total_revenue, " +
                        "        percentage_of_total, " +
                        "        SUM(percentage_of_total) OVER (ORDER BY total_revenue DESC, product_name ASC) AS cumulative_percentage "
                        +
                        "    FROM ProductContribution " +
                        ") " +
                        "SELECT " +
                        "    product_id, " +
                        "    product_name, " +
                        "    total_revenue, " +
                        "    percentage_of_total, " +
                        "    cumulative_percentage, " +
                        "    CASE " +
                        "        WHEN cumulative_percentage <= 80 THEN 'A' " +
                        "        WHEN cumulative_percentage <= 95 THEN 'B' " +
                        "        ELSE 'C' " +
                        "    END AS abc_class " +
                        "FROM CumulativeContribution " +
                        "ORDER BY total_revenue DESC, product_name ASC", nativeQuery = true)
        List<Object[]> performAbcAnalysis(@Param("startDate") ZonedDateTime startDate,
                        @Param("endDate") ZonedDateTime endDate);
}