package com.flick.business.repository;

import com.flick.business.core.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findTop3ByActiveTrueOrderByNameAsc();

    @Query(value = "SELECT p.* FROM products p " +
            "LEFT JOIN sale_items si ON p.id = si.product_id " +
            "WHERE (:name IS NULL OR lower(p.name) LIKE lower(concat('%', :name, '%'))) " +
            "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
            "GROUP BY p.id " +
            "ORDER BY COALESCE(SUM(si.quantity), 0) DESC", nativeQuery = true)
    Page<Product> findAllByMostSold(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query(value = "SELECT p.* FROM products p " +
            "LEFT JOIN sale_items si ON p.id = si.product_id " +
            "WHERE (:name IS NULL OR lower(p.name) LIKE lower(concat('%', :name, '%'))) " +
            "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
            "GROUP BY p.id " +
            "ORDER BY COALESCE(SUM(si.quantity), 0) ASC", nativeQuery = true)
    Page<Product> findAllByLeastSold(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minimumStock")
    List<Product> findLowStockProducts();
}
