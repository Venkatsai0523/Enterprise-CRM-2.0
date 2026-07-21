package com.crm.identity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Role Security Verification", description = "Test endpoints demonstrating @PreAuthorize role protection")
public class TestProtectedController {

    @GetMapping("/authenticated")
    @Operation(summary = "Authenticated user endpoint", description = "Requires any valid JWT token")
    public ResponseEntity<Map<String, String>> authenticated(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "message", "Authenticated successfully",
                "username", userDetails.getUsername()
        ));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin role endpoint", description = "Requires ROLE_ADMIN role")
    public ResponseEntity<Map<String, String>> adminOnly() {
        return ResponseEntity.ok(Map.of("message", "Welcome Admin! Access granted."));
    }

    @GetMapping("/sales-rep")
    @PreAuthorize("hasRole('SALES_REP')")
    @Operation(summary = "Sales Rep role endpoint", description = "Requires ROLE_SALES_REP role")
    public ResponseEntity<Map<String, String>> salesRepOnly() {
        return ResponseEntity.ok(Map.of("message", "Welcome Sales Representative! Access granted."));
    }
}
