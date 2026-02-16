package com.flick.business.api.dto.response.production;

import java.math.BigDecimal;

public record LowStockProductResponse(
  Long id,
  String name,
  BigDecimal currentStock,
  Integer minimumStock
) {}
