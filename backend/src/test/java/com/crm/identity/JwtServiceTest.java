package com.crm.identity;

import com.crm.identity.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        jwtService = new JwtService(secretKey, 3600000L, 604800000L);

        testUser = User.builder()
                .username("rep@nexus.com")
                .password("password123")
                .roles("SALES_REP")
                .build();
    }

    @Test
    @DisplayName("JWT Service: Generate token and extract username")
    void generateAndExtractUsername() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotEmpty();

        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("rep@nexus.com");
    }

    @Test
    @DisplayName("JWT Service: Validate token against matching UserDetails")
    void validateTokenSuccess() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("JWT Service: Validate token fails for mismatched user")
    void validateTokenFailureMismatchedUser() {
        String token = jwtService.generateToken(testUser);

        UserDetails wrongUser = User.builder()
                .username("other@nexus.com")
                .password("password")
                .roles("USER")
                .build();

        boolean isValid = jwtService.isTokenValid(token, wrongUser);
        assertThat(isValid).isFalse();
    }
}
