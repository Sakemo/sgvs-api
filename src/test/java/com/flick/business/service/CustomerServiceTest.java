package com.flick.business.service;

import com.flick.business.api.dto.request.commercial.CustomerRequest;
import com.flick.business.api.mapper.CustomerMapper;
import com.flick.business.core.entity.Customer;
import com.flick.business.core.entity.security.User;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceAlreadyExistsException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.CustomerRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.service.security.AuthenticatedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest customerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .taxId("12345678901")
                .debtBalance(BigDecimal.ZERO)
                .active(true)
                .build();

        customerRequest = new CustomerRequest("Jane Doe", "98765432109", "555-1234", "123 Main St", true,
                new BigDecimal("1000"), true);

        user = User.builder()
                .id(1L)
                .username("test-user")
                .password("123")
                .build();

        lenient().when(authenticatedUserService.getAuthenticatedUserId()).thenReturn(1L);
        lenient().when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
    }

    @Nested
    @DisplayName("Creation and Update")
    class CreationAndUpdate {

        @Test
        @DisplayName("should save a new customer when Tax ID is not duplicated")
        void save_withValidData_isSuccessful() {
            when(customerRepository.findByTaxIdAndUserId(customerRequest.taxId(), 1L)).thenReturn(Optional.empty());
            when(customerMapper.toEntity(customerRequest)).thenReturn(new Customer());
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            customerService.save(customerRequest);

            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("should throw ResourceAlreadyExistsException when saving a customer with a duplicated Tax ID")
        void save_withDuplicatedTaxId_throwsResourceAlreadyExistsException() {
            when(customerRepository.findByTaxIdAndUserId(customerRequest.taxId(), 1L)).thenReturn(Optional.of(new Customer()));

            assertThatThrownBy(() -> customerService.save(customerRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Tax ID already exists");
        }

        @Test
        @DisplayName("should update an existing customer")
        void update_withValidData_isSuccessful() {
            when(customerRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(customer));
            when(customerRepository.findByTaxIdAndUserId(customerRequest.taxId(), 1L)).thenReturn(Optional.empty());
            when(customerRepository.save(customer)).thenReturn(customer);

            customerService.update(1L, customerRequest);

            verify(customerMapper).updateEntityFromRequest(eq(customerRequest), eq(customer));
            verify(customerRepository).save(customer);
        }
    }

    @Nested
    @DisplayName("Listing and Filtering")
    class Listing {

        @Test
        @DisplayName("should call repository with correct specification for filtering active debtors")
        void listAll_withFilters_callsRepositoryWithCorrectSpec() {
            when(customerRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(customer));

            customerService.listAll(null, true, true, "name_asc");

            verify(customerRepository).findAll(any(Specification.class), any(Sort.class));
        }
    }

    @Nested
    @DisplayName("Status and Deletion")
    class StatusAndDeletion {

        @Test
        @DisplayName("should toggle status to inactive when customer has no debt")
        void toggleActiveStatus_withNoDebt_isSuccessful() {
            when(customerRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(customer));

            customerService.toggleActiveStatus(1L, false);

            assertThat(customer.getActive()).isFalse();
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("should throw BusinessException when trying to deactivate a customer with debt")
        void toggleActiveStatus_withDebt_throwsBusinessException() {
            customer.setDebtBalance(new BigDecimal("100.00"));
            when(customerRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(customer));

            assertThatThrownBy(() -> customerService.toggleActiveStatus(1L, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot deactivate a customer with an outstanding debt balance");

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("should permanently delete a customer")
        void deletePermanently_isSuccessful() {
            when(customerRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(customer));

            customerService.deletePermanently(1L);

            verify(customerRepository).delete(customer);
        }
    }
}
