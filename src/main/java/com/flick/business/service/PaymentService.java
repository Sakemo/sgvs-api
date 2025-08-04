package com.flick.business.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.request.commercial.PaymentRequest;
import com.flick.business.core.entity.Customer;
import com.flick.business.core.entity.Payment;
import com.flick.business.core.entity.Sale;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.core.enums.PaymentStatus;
import com.flick.business.exception.BusinessException;
import com.flick.business.repository.PaymentRepository;
import com.flick.business.repository.SaleRepository;
import com.flick.business.core.entity.Sale;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final CustomerService customerService;

    @Transactional
    public void recordPayment(PaymentRequest request) {
        // 1. Validate payment method
        if (request.paymentMethod() == PaymentMethod.ON_CREDIT) {
            throw new BusinessException("Payment method cannot be ON_CREDIT");
        }

        // 2. Search and validate customer
        Customer customer = customerService.findEntityById(request.customerId());

        // 3. Search and validate sales
        List<Sale> salesToSettle = saleRepository.findAllById(request.saleIds());
        validateSales(salesToSettle, request.customerId(), request.amountPaid());

        // 4. Create and save Payment Entity
        Payment newPayment = Payment.builder()
                .customer(customer)
                .amountPaid(request.amountPaid())
                .paymentMethod(request.paymentMethod())
                .settledSales(new HashSet<>(salesToSettle))
                .build();

        // 5. update sale status to PAID
        for (Sale sale : salesToSettle) {
            sale.setPaymentStatus(PaymentStatus.PAID);
        }

        // 5. subtract value from customer credit
        customer.setDebtBalance(customer.getDebtBalance().subtract(request.amountPaid()));
    }

    private void validateSales(List<Sale> sales, Long customerId, BigDecimal amountPaid) {
        if (sales.isEmpty()) {
            throw new BusinessException("No sales found for the provided IDs.");
        }

        BigDecimal totalValueOfSales = BigDecimal.ZERO;
        for (Sale sale : sales) {
            if (!sale.getCustomer().getId().equals(customerId)) {
                throw new BusinessException("Sale with ID " + sale.getId() + " does not belong to the customer");
            }

            if (sale.getPaymentStatus() != PaymentStatus.PENDING) {
                throw new BusinessException("Sale with ID " + sale.getId() + " is not pendind payment");
            }

            totalValueOfSales = totalValueOfSales.add(sale.getTotalValue());
        }

        if (totalValueOfSales.compareTo(amountPaid) != 0) {
            throw new BusinessException("The amount paid " + amountPaid
                    + ") does not match the total value of the selected sales (" + totalValueOfSales + ")");
        }
    }
}
