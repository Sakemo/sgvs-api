package com.flick.business.api.controller;

import com.flick.business.api.dto.request.commercial.ExpenseRequest;
import com.flick.business.api.dto.response.commercial.ExpenseResponse;
import com.flick.business.api.dto.response.common.PageResponse;
import com.flick.business.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            UriComponentsBuilder uriBuilder) {
        ExpenseResponse savedExpense = expenseService.save(request);
        URI uri = uriBuilder.path("/api/expenses/{id}").buildAndExpand(savedExpense.id()).toUri();
        return ResponseEntity.created(uri).body(savedExpense);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ExpenseResponse>> listExpenses(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String expenseType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ExpenseResponse> expensesPage = expenseService.listAll(name, expenseType, startDate, endDate, page,
                size);
        return ResponseEntity.ok(expensesPage);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        // Este método precisa ser criado no seu ExpenseService
        BigDecimal total = expenseService.calculateTotal(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> findExpenseById(@PathVariable Long id) {
        ExpenseResponse expense = expenseService.findById(id);
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse updatedExpense = expenseService.update(id, request);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}