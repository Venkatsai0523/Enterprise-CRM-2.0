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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDto register(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email is already registered: " + dto.getEmail());
        }

        String targetRoleName = (dto.getRoleName() != null && !dto.getRoleName().isBlank())
                ? dto.getRoleName()
                : "ROLE_SALES_REP";

        if (!targetRoleName.startsWith("ROLE_")) {
            targetRoleName = "ROLE_" + targetRoleName;
        }

        Role role = roleRepository.findByName(targetRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + dto.getRoleName()));

        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .enabled(true)
                .roles(Set.of(role))
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public JwtResponseDto login(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        User user = userRepository.findByEmail(dto.getEmail())
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
                User user = userRepository.findByEmail(username)
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
        return userRepository.findByEmail(email).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }
}
