package com.flick.business.api.dto.response.commercial;

import com.flick.business.api.dto.response.summary.CustomerSummaryResponse;
import com.flick.business.api.dto.response.summary.ProductSummaryResponse;
import com.flick.business.core.entity.Sale;
import com.flick.business.core.enums.PaymentMethod;
import com.flick.business.core.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record SaleResponse(
                Long id,
                BigDecimal totalValue,
                CustomerSummaryResponse customer,
                PaymentMethod paymentMethod,
                PaymentStatus paymentStatus,
                String description,
                ZonedDateTime saleDate,
                List<SaleItemResponse> items) {
        public static SaleResponse fromEntity(Sale sale) {
                return new SaleResponse(
                                sale.getId(),
                                sale.getTotalValue(),
                                sale.getCustomer() != null
                                                ? new CustomerSummaryResponse(sale.getCustomer().getId(),
                                                                sale.getCustomer().getName())
                                                : null,
                                sale.getPaymentMethod(),
                                sale.getPaymentStatus(),
                                sale.getDescription(),
                                sale.getSaleDate(),
                                sale.getItems().stream()
                                                .map(item -> new SaleItemResponse(
                                                                item.getId(),
                                                                new ProductSummaryResponse(item.getProduct().getId(),
                                                                                item.getProduct().getName()),
                                                                item.getQuantity(),
                                                                item.getUnitPrice(),
                                                                item.getTotalValue()))
                                                .collect(Collectors.toList()));
        }
}