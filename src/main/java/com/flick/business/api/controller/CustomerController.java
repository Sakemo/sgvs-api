package com.flick.business.api.controller;

import com.flick.business.api.dto.request.CustomerRequest;
import com.flick.business.api.dto.response.CustomerResponse;
import com.flick.business.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers") // << Path em inglês
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request,
            UriComponentsBuilder uriBuilder) {
        CustomerResponse savedCustomer = customerService.save(request);
        URI uri = uriBuilder.path("/api/customers/{id}").buildAndExpand(savedCustomer.id()).toUri();
        return ResponseEntity.created(uri).body(savedCustomer);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> listCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean hasDebt,
            @RequestParam(required = false) String orderBy) {
        List<CustomerResponse> customers = customerService.listAll(name, isActive, hasDebt, orderBy);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findCustomerById(@PathVariable Long id) {
        CustomerResponse customer = customerService.findById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<CustomerResponse>> getCustomerSuggestions() {
        return ResponseEntity.ok(customerService.getSuggestions());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse updatedCustomer = customerService.update(id, request);
        return ResponseEntity.ok(updatedCustomer);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleCustomerStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Boolean active = payload.get("active");
        if (active == null) {
            // Lançar um bad request ou similar
            return ResponseEntity.badRequest().build();
        }
        customerService.toggleActiveStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deletePermanently(id);
        return ResponseEntity.noContent().build();
    }
}