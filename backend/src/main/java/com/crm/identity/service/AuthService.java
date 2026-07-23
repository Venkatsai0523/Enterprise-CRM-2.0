package com.crm.identity.service;

import com.crm.common.exception.BadRequestException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.identity.api.IdentityApi;
import com.crm.identity.api.dto.*;
import com.crm.identity.entity.Role;
import com.crm.identity.entity.User;
import com.crm.identity.mapper.UserMapper;
import com.crm.identity.repository.RoleRepository;
import com.crm.identity.repository.UserRepository;
import com.crm.organization.entity.Organization;
import com.crm.organization.repository.OrganizationRepository;
import com.crm.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class AuthService implements IdentityApi {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserMapper userMapper;
    private final TransactionTemplate transactionTemplate;

    public UserResponseDto register(UserRegistrationDto dto) {
        if (userRepository.existsByEmailBypassingTenant(dto.getEmail())) {
            throw new BadRequestException("Email is already registered: " + dto.getEmail());
        }

        // --- Resolve role ---
        String targetRoleName = (dto.getRoleName() != null && !dto.getRoleName().isBlank())
                ? dto.getRoleName()
                : "ROLE_SALES_REP";
        if (!targetRoleName.startsWith("ROLE_")) {
            targetRoleName = "ROLE_" + targetRoleName;
        }
        final String resolvedRoleName = targetRoleName;
        Role role = roleRepository.findByName(resolvedRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + resolvedRoleName));

        // --- Validate org name/subdomain uniqueness ---
        String orgName = dto.getOrganizationName().trim();
        String subdomain = (dto.getSubdomain() != null && !dto.getSubdomain().isBlank())
                ? dto.getSubdomain().toLowerCase().trim()
                : orgName.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");

        if (organizationRepository.existsByName(orgName)) {
            throw new BadRequestException("Organization name already taken: " + orgName);
        }
        if (organizationRepository.existsBySubdomain(subdomain)) {
            throw new BadRequestException("Subdomain already taken: " + subdomain);
        }

        // 1. Create Organization in its own transaction
        Organization organization = transactionTemplate.execute(status ->
                organizationRepository.saveAndFlush(
                        Organization.builder()
                                .name(orgName)
                                .subdomain(subdomain)
                                .active(true)
                                .build()
                )
        );

        UUID orgId = organization.getId();
        log.info("Created organization '{}' with id={} for new user {}", orgName, orgId, dto.getEmail());

        // 2. Set TenantContext to orgId BEFORE opening the User transaction
        TenantContext.setTenantId(orgId);
        try {
            return transactionTemplate.execute(status -> {
                User user = User.builder()
                        .email(dto.getEmail())
                        .passwordHash(passwordEncoder.encode(dto.getPassword()))
                        .firstName(dto.getFirstName())
                        .lastName(dto.getLastName())
                        .enabled(true)
                        .roles(Set.of(role))
                        .organizationId(orgId)
                        .build();

                User savedUser = userRepository.saveAndFlush(user);
                return userMapper.toDto(savedUser);
            });
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public UserResponseDto createUser(UserCreateDto dto, UUID orgId) {
        if (userRepository.existsByEmailBypassingTenant(dto.getEmail())) {
            throw new BadRequestException("Email is already registered: " + dto.getEmail());
        }

        // --- Resolve role ---
        String targetRoleName = dto.getRoleName().trim();
        if (!targetRoleName.startsWith("ROLE_")) {
            targetRoleName = "ROLE_" + targetRoleName;
        }
        final String resolvedRoleName = targetRoleName;
        Role role = roleRepository.findByName(resolvedRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + resolvedRoleName));

        // --- Build and save user ---
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .enabled(true)
                .roles(Set.of(role))
                .organizationId(orgId)
                .build();

        TenantContext.setTenantId(orgId);
        try {
            User savedUser = userRepository.saveAndFlush(user);
            log.info("Created user '{}' for organization id={} by admin", dto.getEmail(), orgId);
            return userMapper.toDto(savedUser);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public JwtResponseDto login(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        User user = userRepository.findByEmailBypassingTenant(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getEmail()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return JwtResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .roles(roleNames)
                .build();
    }

    @Transactional(readOnly = true)
    public JwtResponseDto refreshToken(TokenRefreshRequestDto dto) {
        String refreshToken = dto.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                User user = userRepository.findByEmailBypassingTenant(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

                String newAccessToken = jwtService.generateToken(userDetails);
                String newRefreshToken = jwtService.generateRefreshToken(userDetails);

                Set<String> roleNames = user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet());

                return JwtResponseDto.builder()
                        .token(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .type("Bearer")
                        .id(user.getId())
                        .email(user.getEmail())
                        .roles(roleNames)
                        .build();
            }
        }
        throw new BadRequestException("Invalid or expired refresh token");
    }

    // --- IdentityApi Implementation ---

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findUserById(UUID userId) {
        return userRepository.findById(userId).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findUserByEmail(String email) {
        return userRepository.findByEmailBypassingTenant(email).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserResponseDto> findActiveUsersByRole(String roleName) {
        return userRepository.findActiveUsersByRole(roleName).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
