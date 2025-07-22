package com.flick.business.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.request.CustomerRequest;
import com.flick.business.api.dto.response.CustomerResponse;
import com.flick.business.api.mapper.CustomerMapper;
import com.flick.business.core.entity.Customer;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceAlreadyExistsException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.CustomerRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.repository.spec.CustomerSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final SaleRepository saleRepository;

    @Transactional
    public CustomerResponse save(CustomerRequest request) {
        validateTaxId(request.taxId(), null);
        Customer customer = customerMapper.toEntity(request);

        System.out.println("SERVICE: Updating customer. Credit Limit from DTO: " + request.creditLimit());

        Customer savedCustomer = customerRepository.save(customer);

        System.out.println("SERVICE: Entity after save. Credit Limit: " + savedCustomer.getCreditLimit());

        return CustomerResponse.fromEntity(savedCustomer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getSuggestions() {
        List<Long> topIds = saleRepository.findTop3MostFrequentCustomerIds();

        List<Customer> customers;
        if (!topIds.isEmpty()) {
            customers = customerRepository.findAllById(topIds);
        } else {
            customers = customerRepository.findTop3ByActiveTrueOrderByNameAsc();
        }

        return customers.stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer existingCustomer = findEntityById(id);
        validateTaxId(request.taxId(), id);

        System.out.println("SERVICE: Updating customer. Credit Limit from DTO: " + request.creditLimit());

        customerMapper.updateEntityFromRequest(request, existingCustomer);
        Customer updatedCustomer = customerRepository.save(existingCustomer);

        System.out.println("SERVICE: Entity after save. Credit Limit: " + updatedCustomer.getCreditLimit());
        return CustomerResponse.fromEntity(updatedCustomer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listAll(String name, Boolean isActive, Boolean hasDebt, String orderBy) {
        Sort sort = createSort(orderBy);
        Specification<Customer> spec = CustomerSpecification.withFilters(name, isActive, hasDebt);
        List<Customer> customers = customerRepository.findAll(spec, sort);
        return customers.stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleActiveStatus(Long id, boolean active) {
        Customer customer = findEntityById(id);
        if (!active && customer.getDebtBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Cannot deactivate a customer with an outstanding debt balance.");
        }
        customer.setActive(active);
        customerRepository.save(customer);
    }

    @Transactional
    public void deletePermanently(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
    }

    private void validateTaxId(String taxId, Long currentCustomerId) {
        if (taxId == null || taxId.isBlank()) {
            return;
        }
        Optional<Customer> existingCustomer = customerRepository.findByTaxId(taxId);
        if (existingCustomer.isPresent()
                && (currentCustomerId == null || !existingCustomer.get().getId().equals(currentCustomerId))) {
            throw new ResourceAlreadyExistsException("A customer with this Tax ID already exists: " + taxId);
        }
    }

    private Sort createSort(String orderBy) {
        if (orderBy == null || orderBy.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }
        return switch (orderBy) {
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "name");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "debtBalance");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "debtBalance");
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return CustomerResponse.fromEntity(findEntityById(id));
    }

    public Customer findEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found  with ID: " + id));
    }
}
