package com.crm.identity.controller;

import com.crm.identity.api.dto.*;
import com.crm.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Access", description = "Endpoints for user registration, authentication, and JWT issuance")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with BCrypt password hashing and returns user profile DTO")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto dto) {
        java.util.UUID orgId = dto.getOrganizationId() != null 
                ? dto.getOrganizationId() 
                : com.crm.infrastructure.tenant.TenantContextResolver.DEFAULT_ORG_ID;

        UserResponseDto response = com.crm.infrastructure.tenant.TenantContext.computeInTenantContext(
                orgId, 
                () -> authService.register(dto)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user credentials and returns JWT access and refresh tokens")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        JwtResponseDto response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token", description = "Generates new JWT access and refresh tokens using a valid refresh token")
    public ResponseEntity<JwtResponseDto> refreshToken(@Valid @RequestBody TokenRefreshRequestDto dto) {
        JwtResponseDto response = authService.refreshToken(dto);
        return ResponseEntity.ok(response);
    }
}
