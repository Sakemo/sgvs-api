package com.flick.business.core.entity;

import com.flick.business.core.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @CreationTimestamp
    @Column(name = "payment_date", nullable = false, updatable = false)
    private ZonedDateTime paymentDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "payment_sales", joinColumns = @JoinColumn(name = "payment_id"), inverseJoinColumns = @JoinColumn(name = "sale_id"))
    @Builder.Default
    private Set<Sale> settledSales = new HashSet<>();
}