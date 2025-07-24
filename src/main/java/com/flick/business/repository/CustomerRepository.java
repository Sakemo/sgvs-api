package com.flick.business.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flick.business.core.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    /**
     * Finds a customer by their tax ID.
     * 
     * @param taxId The tax ID to search for.
     * @return An Optional containing the found customer or an empty Optional if not
     *         found.
     */
    Optional<Customer> findByTaxId(String taxId);

    List<Customer> findTop3ByActiveTrueOrderByNameAsc();

    @Query("SELECT COALESCE(SUM(c.debtBalance), 0) FROM Customer c WHERE c.active = true")
    BigDecimal findTotalDebtBalance();

    @Query("SELECT COUNT(c.id) FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Long countNewCustomersBetween(@Param("startDate") ZonedDateTime startDate, @Param("endDate") ZonedDateTime endDate);
}