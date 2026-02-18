package com.flick.business.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.flick.business.core.entity.security.User;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 11, unique = true)
    private String taxId;

    @Column(length = 11)
    private String phone;

    @Column(length = 150)
    private String address;

    @Column(name = "credit_enabled", nullable = false)
    @Builder.Default
    private Boolean creditEnabled = false;

    @Column(name = "credit_limit", precision = 10, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "debt_balance", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal debtBalance = BigDecimal.ZERO;

    @Column(name = "last_credit_purchase_at")
    private ZonedDateTime lastCreditPurchaseAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
