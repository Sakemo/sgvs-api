package com.flick.business.api.dto.response.common;

import java.math.BigDecimal;

import com.flick.business.core.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TotalByPaymentMethod {
    private PaymentMethod paymentMethod;
    private BigDecimal total;
}
