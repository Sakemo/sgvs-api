package com.flick.business.core.entity;

import com.flick.business.core.enums.UnitOfSale;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(length = 50)
    private String barcode;

    @Column(name = "stock_quantity", nullable = false)
    private BigDecimal stockQuantity;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "minimum_stock")
    private Integer minimumStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_sale", nullable = false)
    private UnitOfSale unitOfSale;

    @Column(name = "manages_stock", nullable = false)
    @Builder.Default
    private boolean manageStock = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;
}
