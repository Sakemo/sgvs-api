package com.flick.business.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}