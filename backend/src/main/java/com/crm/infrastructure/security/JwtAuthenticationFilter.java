package com.crm.infrastructure.security;

import com.crm.identity.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            if (!jwtService.isTokenExpired(jwt)) {
                String userEmail = jwtService.extractUsername(jwt);
                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String orgIdStr = jwtService.extractClaim(jwt, "organizationId");
                    String userIdStr = jwtService.extractClaim(jwt, "userId");
                    java.util.List<String> roles = jwtService.extractRoles(jwt);

                    if (orgIdStr != null && userIdStr != null && roles != null) {
                        java.util.UUID organizationId = java.util.UUID.fromString(orgIdStr);
                        java.util.UUID userId = java.util.UUID.fromString(userIdStr);

                        java.util.Set<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = roles.stream()
                                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                                .collect(java.util.stream.Collectors.toSet());

                        com.crm.identity.api.dto.CustomUserDetails userDetails = com.crm.identity.api.dto.CustomUserDetails.builder()
                                .userId(userId)
                                .email(userEmail)
                                .passwordHash("") // stateless
                                .enabled(true)
                                .organizationId(organizationId)
                                .authorities(authorities)
                                .build();

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Set TenantContext for the thread
                        com.crm.infrastructure.tenant.TenantContext.setTenantId(organizationId);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        } finally {
            com.crm.infrastructure.tenant.TenantContext.clear();
        }
    }
}
