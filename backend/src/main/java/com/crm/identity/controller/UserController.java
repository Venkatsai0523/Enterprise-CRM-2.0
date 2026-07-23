package com.crm.identity.controller;

import com.crm.identity.api.dto.UserResponseDto;
import com.crm.identity.api.dto.UserCreateDto;
import com.crm.identity.api.dto.CustomUserDetails;
import com.crm.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile & Identity Management", description = "Endpoints for managing user accounts and retrieving profiles")
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile information of the currently authenticated user")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return authService.findUserByEmail(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Allows Admin to invite/create Manager or Sales Rep users in their organization")
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponseDto response = authService.createUser(dto, userDetails.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
