package com.flick.business.repository;

import com.flick.business.core.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("SELECT si.product.id FROM SaleItem si GROUP BY si.product.id ORDER BY SUM(si.quantity) DESC LIMIT 3")
    List<Long> findTop3MostSoldProductIds();
}