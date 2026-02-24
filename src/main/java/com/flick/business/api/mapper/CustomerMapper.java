package com.flick.business.api.mapper;

import com.flick.business.api.dto.request.commercial.CustomerRequest;
import com.flick.business.core.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .name(request.name())
                .taxId(request.taxId())
                .phone(request.phone())
                .address(request.address())
                .creditEnabled(request.creditEnabled())
                .creditLimit(request.creditLimit())
                .active(request.active())
                .build();
    }

    public void updateEntityFromRequest(CustomerRequest request, Customer customer) {
        customer.setName(request.name());
        customer.setTaxId(request.taxId());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        customer.setCreditEnabled(request.creditEnabled());
        customer.setCreditLimit(request.creditLimit());
        customer.setActive(request.active());
    }
}
