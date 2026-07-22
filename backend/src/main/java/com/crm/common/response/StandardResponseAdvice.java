package com.crm.common.response;

import com.crm.common.exception.ErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import java.time.Instant;

@RestControllerAdvice
public class StandardResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> parameterType = returnType.getParameterType();
        return !StandardResponse.class.isAssignableFrom(parameterType)
                && !ErrorResponse.class.isAssignableFrom(parameterType);
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {

        String path = request.getURI().getPath();

        // Skip OpenAPI/Swagger UI resources and Actuator
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/actuator")) {
            return body;
        }

        // Skip if the response is an ErrorResponse
        if (body instanceof ErrorResponse) {
            return body;
        }

        // Skip if the response is already a StandardResponse
        if (body instanceof StandardResponse) {
            return body;
        }

        // Avoid ClassCastException for String responses
        if (body instanceof String) {
            return body;
        }

        return StandardResponse.builder()
                .success(true)
                .message("Operation completed successfully.")
                .data(body)
                .timestamp(Instant.now())
                .path(path)
                .build();
    }
}
