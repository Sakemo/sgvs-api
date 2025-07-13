package com.flick.business.api.dto.response;

import com.flick.business.api.dto.response.summary.ProductSummaryResponse;
import java.math.BigDecimal;

public record SaleItemResponse(
        Long id,
        ProductSummaryResponse product,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalValue) {
}