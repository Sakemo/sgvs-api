package com.flick.business.service;

import com.flick.business.api.dto.request.SaleItemRequest;
import com.flick.business.api.dto.request.SaleRequest;
import com.flick.business.api.dto.response.SaleResponse;
import com.flick.business.core.entity.Customer;
import com.flick.business.core.entity.Product;
import com.flick.business.core.entity.Sale;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceNotFoundException;
import com.flick.business.repository.CustomerRepository;
import com.flick.business.repository.ProductRepository;
import com.flick.business.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sale Service Tests")
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductService productService;
    @Mock
    private CustomerService customerService;

    @InjectMocks
    private SaleService saleService;

    private Product product1;
    private Customer onCreditCustomer;
    private Customer cashCustomer;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(1L)
                .name("Test Product")
                .salePrice(new BigDecimal("10.00"))
                .stockQuantity(new BigDecimal("100"))
                .build();

        onCreditCustomer = Customer.builder()
                .id(1L)
                .name("On Credit Customer")
                .creditEnabled(true)
                .creditLimit(new BigDecimal("500.00"))
                .debtBalance(BigDecimal.ZERO)
                .build();

        cashCustomer = Customer.builder()
                .id(2L)
                .name("Cash Customer")
                .creditEnabled(false)
                .build();
    }

    @Nested
    @DisplayName("Sale Registration Scenarios")
    class RegisterSale {

        @Test
        @DisplayName("should register an ON_CREDIT sale successfully")
        void registerSale_onCreditSale_isSuccessful() {
            SaleItemRequest itemRequest = new SaleItemRequest(1L, new BigDecimal("5"));
            SaleRequest saleRequest = new SaleRequest(1L, PaymentMethod.ON_CREDIT, "Test sale", List.of(itemRequest));

            when(customerService.findEntityById(1L)).thenReturn(onCreditCustomer);
            when(productService.findEntityById(1L)).thenReturn(product1);
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            SaleResponse result = saleService.registerSale(saleRequest);

            assertThat(result).isNotNull();
            assertThat(result.totalValue()).isEqualByComparingTo("50.00");
            assertThat(onCreditCustomer.getDebtBalance()).isEqualByComparingTo("50.00");
            assertThat(product1.getStockQuantity()).isEqualByComparingTo("95");

            verify(productRepository, times(1)).saveAll(anyList());
            verify(customerRepository, times(1)).save(onCreditCustomer);
            verify(saleRepository, times(1)).save(any(Sale.class));
        }

        @Test
        @DisplayName("should register a CASH sale successfully without a customer")
        void registerSale_cashSaleWithoutCustomer_isSuccessful() {
            SaleItemRequest itemRequest = new SaleItemRequest(1L, new BigDecimal("2"));
            SaleRequest saleRequest = new SaleRequest(null, PaymentMethod.CASH, "Cash sale", List.of(itemRequest));

            when(productService.findEntityById(1L)).thenReturn(product1);
            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

            SaleResponse result = saleService.registerSale(saleRequest);

            assertThat(result).isNotNull();
            assertThat(result.totalValue()).isEqualByComparingTo("20.00");
            assertThat(product1.getStockQuantity()).isEqualByComparingTo("98");

            verify(customerService, never()).findEntityById(any());
            verify(customerRepository, never()).save(any());
            verify(saleRepository, times(1)).save(any(Sale.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for a non-existent product")
        void registerSale_withNonExistentProduct_throwsResourceNotFoundException() {
            when(productService.findEntityById(99L)).thenThrow(new ResourceNotFoundException("Product not found"));
            SaleItemRequest itemRequest = new SaleItemRequest(99L, new BigDecimal("1"));
            SaleRequest saleRequest = new SaleRequest(null, PaymentMethod.CASH, "", List.of(itemRequest));

            assertThatThrownBy(() -> saleService.registerSale(saleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("should throw BusinessException for insufficient stock")
        void registerSale_withInsufficientStock_throwsBusinessException() {
            product1.setStockQuantity(new BigDecimal("2"));
            when(productService.findEntityById(1L)).thenReturn(product1);
            SaleItemRequest itemRequest = new SaleItemRequest(1L, new BigDecimal("5"));
            SaleRequest saleRequest = new SaleRequest(null, PaymentMethod.CASH, "", List.of(itemRequest));

            assertThatThrownBy(() -> saleService.registerSale(saleRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Insufficient stock");

            verifyNoInteractions(saleRepository, customerRepository);
        }

        @Test
        @DisplayName("should throw BusinessException for ON_CREDIT sale without a customer")
        void registerSale_onCreditWithoutCustomer_throwsBusinessException() {
            SaleItemRequest itemRequest = new SaleItemRequest(1L, new BigDecimal("1"));
            SaleRequest saleRequest = new SaleRequest(null, PaymentMethod.ON_CREDIT, "", List.of(itemRequest));

            assertThatThrownBy(() -> saleService.registerSale(saleRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Customer is required for ON_CREDIT sales");
        }

        @Test
        @DisplayName("should throw BusinessException for ON_CREDIT sale to a customer not enabled for credit")
        void registerSale_onCreditToCustomerNotEnabled_throwsBusinessException() {
            when(customerService.findEntityById(2L)).thenReturn(cashCustomer);
            when(productService.findEntityById(1L)).thenReturn(product1);
            SaleItemRequest itemRequest = new SaleItemRequest(1L, new BigDecimal("1"));
            SaleRequest saleRequest = new SaleRequest(2L, PaymentMethod.ON_CREDIT, "", List.of(itemRequest));

            assertThatThrownBy(() -> saleService.registerSale(saleRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("This customer is not enabled for credit purchases");
        }

        @Test
        @DisplayName("should throw BusinessException when ON_CREDIT sale exceeds credit limit")
        void registerSale_onCreditExceedingLimit_throwsBusinessException() {
            onCreditCustomer.setCreditLimit(new BigDecimal("40.00"));
            when(customerService.findEntityById(1L)).thenReturn(onCreditCustomer);
            when(productService.findEntityById(1L)).thenReturn(product1);
            SaleItemRequest itemRequest = new SaleItemRequest(1L, new BigDecimal("5"));
            SaleRequest saleRequest = new SaleRequest(1L, PaymentMethod.ON_CREDIT, "", List.of(itemRequest));

            assertThatThrownBy(() -> saleService.registerSale(saleRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Credit limit exceeded");
        }

        @Test
        @DisplayName("should throw Exception when item list is empty")
        void registerSale_withEmptyItemList_throwsException() {
            SaleRequest saleRequest = new SaleRequest(1L, PaymentMethod.CASH, "", Collections.emptyList());

            // A validação @NotEmpty no DTO deve pegar isso, mas podemos testar o
            // comportamento do serviço se ele receber.
            // O comportamento exato pode variar. Se for uma IllegalArgumentException, por
            // exemplo:
            assertThatThrownBy(() -> saleService.registerSale(saleRequest))
                    .isInstanceOf(Exception.class); // Pode ser mais específico dependendo da sua validação
        }
    }
}