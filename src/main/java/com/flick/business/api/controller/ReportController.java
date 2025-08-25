package com.flick.business.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flick.business.api.dto.response.aministration.reports.AbcAnalysisResponse;
import com.flick.business.api.dto.response.reports.FinancialSummaryResponse;
import com.flick.business.service.ReportService;

import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/financial-summary")
    public ResponseEntity<FinancialSummaryResponse> getFinancialSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        FinancialSummaryResponse summary = reportService.getFinancialSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/abc-analysis")
    public ResponseEntity<List<AbcAnalysisResponse>> getAbcAnalisys(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        List<AbcAnalysisResponse> report = reportService.getAbcAnalisys(startDate, endDate);
        return ResponseEntity.ok(report);
    }

}
