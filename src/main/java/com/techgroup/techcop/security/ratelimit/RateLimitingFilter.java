package com.techgroup.techcop.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgroup.techcop.exception.ApiErrorResponse;
import com.techgroup.techcop.security.util.ClientIpResolver;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final String LOGIN_POLICY = "login";
    private static final String API_POLICY = "api";
    private static final long LOGIN_LIMIT = 5;
    private static final long API_LIMIT = 60;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) || resolvePolicy(request) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String policy = resolvePolicy(request);
        String clientIp = ClientIpResolver.resolve(request);
        String bucketKey = policy + ":" + clientIp;

        Bucket bucket = buckets.computeIfAbsent(bucketKey, key -> createBucket(policy));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));

        if (!probe.isConsumed()) {
            log.warn("Rate limit excedido para politica {} desde IP {}", policy, clientIp);
            ApiErrorResponse body = new ApiErrorResponse(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Demasiadas solicitudes. Intenta de nuevo en un minuto.",
                    List.of(),
                    Instant.now(),
                    request.getRequestURI()
            );
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Bucket createBucket(String policy) {
        long limit = LOGIN_POLICY.equals(policy) ? LOGIN_LIMIT : API_LIMIT;

        return Bucket.builder()
                .addLimit(Bandwidth.classic(limit, Refill.intervally(limit, WINDOW)))
                .build();
    }

    private String resolvePolicy(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String loginPath = contextPath + "/auth/login";
        String apiPrefix = contextPath + "/";

        if (requestUri.equals(loginPath)) {
            return LOGIN_POLICY;
        }

        if (requestUri.startsWith(apiPrefix)) {
            return API_POLICY;
        }

        return null;
    }
}
