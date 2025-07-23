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
}