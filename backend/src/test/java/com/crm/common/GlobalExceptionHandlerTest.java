package com.crm.common;

import com.crm.common.exception.BadRequestException;
import com.crm.common.exception.ErrorResponse;
import com.crm.common.exception.GlobalExceptionHandler;
import com.crm.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    @DisplayName("Exception Handler: BadRequestException returns HTTP 400 ErrorResponse")
    void handleBadRequestException() {
        BadRequestException ex = new BadRequestException("Invalid input provided");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        if (body != null) {
            assertThat(body.getStatus()).isEqualTo(400);
            assertThat(body.getMessage()).isEqualTo("Invalid input provided");
            assertThat(body.getPath()).isEqualTo("/api/test");
        }
    }

    @Test
    @DisplayName("Exception Handler: ResourceNotFoundException returns HTTP 404 ErrorResponse")
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Lead not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        if (body != null) {
            assertThat(body.getStatus()).isEqualTo(404);
            assertThat(body.getMessage()).isEqualTo("Lead not found");
            assertThat(body.getPath()).isEqualTo("/api/test");
        }
    }
}
