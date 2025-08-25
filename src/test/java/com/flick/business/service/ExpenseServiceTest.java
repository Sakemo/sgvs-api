package com.flick.business.service;

import com.flick.business.api.dto.request.commercial.ExpenseRequest;
import com.flick.business.api.dto.response.production.RestockItemRequest;
import com.flick.business.core.entity.Expense;
import com.flick.business.core.entity.Product;
import com.flick.business.core.enums.ExpenseType;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.exception.BusinessException;
import com.flick.business.repository.ExpenseRepository;
import com.flick.business.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Expense Service Tests")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductService productService;

    @InjectMocks
    private ExpenseService expenseService;

    @Captor
    private ArgumentCaptor<Expense> expenseArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<Product>> productListArgumentCaptor;

    private Product product1;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(1L)
                .name("Test Product")
                .stockQuantity(new BigDecimal("10"))
                .costPrice(new BigDecimal("5.00"))
                .build();
    }

    @Nested
    @DisplayName("Expense Creation")
    class CreateExpense {

        @Test
        @DisplayName("should save a simple expense successfully")
        void createExpense_forSimpleExpense_isSuccessful() {
            ExpenseRequest request = new ExpenseRequest("Office Rent", new BigDecimal("1500.00"), ZonedDateTime.now(),
                    ExpenseType.BUSINESS, PaymentMethod.BANK_TRANSFER, "Monthly rent", null);
            when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

            expenseService.create(request);

            verify(expenseRepository).save(expenseArgumentCaptor.capture());
            Expense savedExpense = expenseArgumentCaptor.getValue();

            assertThat(savedExpense.getValue()).isEqualByComparingTo("1500.00");
            assertThat(savedExpense.getExpenseType()).isEqualTo(ExpenseType.BUSINESS);
            assertThat(savedExpense.getRestockItems()).isEmpty();
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("should save a restocking expense and update product stock")
        void createExpense_forRestockingExpense_updatesStockAndCalculatesValue() {
            RestockItemRequest item = new RestockItemRequest(1L, new BigDecimal("50"), new BigDecimal("4.50"));
            ExpenseRequest request = new ExpenseRequest("Supplier A purchase", null, ZonedDateTime.now(),
                    ExpenseType.RESTOCKING, PaymentMethod.CASH, "New stock", List.of(item));

            when(productService.findEntityById(1L)).thenReturn(product1);
            when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

            expenseService.create(request);

            verify(expenseRepository).save(expenseArgumentCaptor.capture());
            verify(productRepository).saveAll(productListArgumentCaptor.capture());

            Expense savedExpense = expenseArgumentCaptor.getValue();
            assertThat(savedExpense.getValue()).isEqualByComparingTo("225.00"); // 50 * 4.50
            assertThat(savedExpense.getRestockItems()).hasSize(1);

            Product updatedProduct = productListArgumentCaptor.getValue().get(0);
            assertThat(updatedProduct.getStockQuantity()).isEqualByComparingTo("60"); // 10 + 50
            assertThat(updatedProduct.getCostPrice()).isEqualByComparingTo("5.00");
        }

        @Test
        @DisplayName("should throw BusinessException for restocking expense with no items")
        void createExpense_restockingWithNoItems_throwsBusinessException() {
            ExpenseRequest request = new ExpenseRequest("Invalid Restock", null, ZonedDateTime.now(),
                    ExpenseType.RESTOCKING, PaymentMethod.CASH, "", Collections.emptyList());

            assertThatThrownBy(() -> expenseService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Restocking expenses must contain at least one item");
        }

        @Test
        @DisplayName("should throw BusinessException for simple expense with no value")
        void createExpense_simpleWithNoValue_throwsBusinessException() {
            ExpenseRequest request = new ExpenseRequest("Invalid Simple", null, ZonedDateTime.now(),
                    ExpenseType.BUSINESS, PaymentMethod.CASH, "", null);

            assertThatThrownBy(() -> expenseService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("value greater than zero is required");
        }
    }
}