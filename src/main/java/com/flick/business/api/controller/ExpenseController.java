package com.flick.business.api.controller;

import com.flick.business.api.dto.request.ExpenseRequest;
import com.flick.business.api.dto.response.ExpenseResponse;
import com.flick.business.api.dto.response.PageResponse;
import com.flick.business.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

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
            @RequestParam(defaultValue = "0") int page, // << Adicionar
            @RequestParam(defaultValue = "10") int size // << Adicionar
    ) {
        PageResponse<ExpenseResponse> expensesPage = expenseService.listAll(name, expenseType, startDate, endDate, page,
                size);
        return ResponseEntity.ok(expensesPage);
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