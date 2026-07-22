package com.crm.customer.controller;

import com.crm.customer.api.dto.Customer360ResponseDto;
import com.crm.customer.api.dto.CustomerAccountResponseDto;
import com.crm.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer 360", description = "Endpoints for Customer Account management and 360-degree customer view")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "Get Customer 360 View", description = "Retrieves full 360-degree account view including all linked won opportunities and lifetime revenue")
    public ResponseEntity<Customer360ResponseDto> getCustomer360(@PathVariable UUID id) {
        Customer360ResponseDto response = customerService.getCustomer360(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_REP', 'MANAGER')")
    @Operation(summary = "List customer accounts", description = "Retrieves paginated list of customer accounts. Max page size: 100.")
    public ResponseEntity<Page<CustomerAccountResponseDto>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @jakarta.validation.constraints.Max(value = 100, message = "Page size must not exceed 100") @RequestParam(defaultValue = "10") int size
    ) {
        Page<CustomerAccountResponseDto> response = customerService.getCustomerAccounts(page, size);
        return ResponseEntity.ok(response);
    }
}
