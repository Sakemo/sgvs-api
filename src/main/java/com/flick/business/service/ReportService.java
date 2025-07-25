package com.flick.business.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flick.business.api.dto.response.aministration.reports.AbcAnalysisResponse;
import com.flick.business.repository.SaleItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final SaleItemRepository saleItemRepository;

    @Transactional(readOnly = true)
    public List<AbcAnalysisResponse> getAbcAnalisys(ZonedDateTime startDate, ZonedDateTime endDate) {
        List<Object[]> rawResults = saleItemRepository.performAbcAnalysis(startDate, endDate);

        return rawResults.stream()
                .map(row -> new AbcAnalysisResponse(
                        ((Number) row[0]).longValue(), // productId
                        (String) row[1], // productName
                        (BigDecimal) row[2], // totalRevenue
                        ((Number) row[3]).doubleValue(), // percentageOfTotalRevenue
                        ((Number) row[4]).doubleValue(), // cumulativePercentage
                        (String) row[5] // abcClass
                ))
                .collect(Collectors.toList());
    }
}
