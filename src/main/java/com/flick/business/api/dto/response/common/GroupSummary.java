package com.flick.business.api.dto.response.common;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupSummary {
    private String groupKey;
    private String groupTitle;
    private BigDecimal totalValue;
}
