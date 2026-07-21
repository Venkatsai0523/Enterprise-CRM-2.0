package com.crm.identity.api;

import com.crm.identity.api.dto.UserResponseDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Published cross-domain interface for Identity & Access domain.
 * External domains MUST use this interface and DTOs rather than importing entity or repository classes directly.
 */
public interface IdentityApi {

    Optional<UserResponseDto> findUserById(UUID userId);

    Optional<UserResponseDto> findUserByEmail(String email);

    boolean existsById(UUID userId);
}
