package com.flick.business.api.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.flick.business.core.entity.Customer;

public record CustomerResponse(
        Long id,
        String name,
        String taxId,
        String phone,
        String address,
        Boolean creditEnabled,
        BigDecimal debtBalance,
        ZonedDateTime lastCreditPurchaseAt,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        Boolean active) {
    public static CustomerResponse fromEntity(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getTaxId(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCreditEnabled(),
                customer.getDebtBalance(),
                customer.getLastCreditPurchaseAt(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getActive());
    }
}