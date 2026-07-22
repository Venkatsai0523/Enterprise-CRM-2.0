package com.crm.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String roleName; // e.g., ROLE_SALES_REP or ROLE_ADMIN

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    /**
     * URL-safe subdomain for the organization (e.g. "acme-corp").
     * Auto-derived from organizationName if not provided.
     */
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Subdomain must be lowercase letters, digits or hyphens only")
    private String subdomain;
}
