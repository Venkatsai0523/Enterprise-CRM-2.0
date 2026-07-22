package com.crm.infrastructure.idempotency;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyFilter implements Filter {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI();
        String idempotencyKey = httpRequest.getHeader("Idempotency-Key");

        boolean isTarget = "POST".equalsIgnoreCase(method) && 
                (path.startsWith("/api/leads") || path.startsWith("/api/opportunities") || path.startsWith("/api/tasks"));

        if (!isTarget || idempotencyKey == null || idempotencyKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        String redisKey = "idempotency:" + idempotencyKey;

        // Try to acquire lock / detect duplicate
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", 5, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(acquired)) {
            String cachedValue = redisTemplate.opsForValue().get(redisKey);
            if ("PROCESSING".equals(cachedValue)) {
                httpResponse.setStatus(HttpServletResponse.SC_CONFLICT);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Conflict\",\"message\":\"A request with the same Idempotency-Key is already being processed.\"}");
                return;
            }

            if (cachedValue != null && cachedValue.contains("|#|")) {
                int separatorIndex = cachedValue.indexOf("|#|");
                int statusCode = Integer.parseInt(cachedValue.substring(0, separatorIndex));
                String body = cachedValue.substring(separatorIndex + 3);

                httpResponse.setStatus(statusCode);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(body);
                log.info("Replayed idempotent response for key: {}", idempotencyKey);
                return;
            }

            redisTemplate.opsForValue().set(redisKey, "PROCESSING", 5, TimeUnit.MINUTES);
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);
        try {
            chain.doFilter(request, responseWrapper);
            
            int status = responseWrapper.getStatus();
            byte[] responseBodyBytes = responseWrapper.getContentAsByteArray();
            String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);

            if (status >= 200 && status < 500) {
                String valToStore = status + "|#|" + responseBody;
                redisTemplate.opsForValue().set(redisKey, valToStore, 24, TimeUnit.HOURS);
            } else {
                redisTemplate.delete(redisKey);
            }
        } catch (Exception ex) {
            redisTemplate.delete(redisKey);
            throw ex;
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }
}
