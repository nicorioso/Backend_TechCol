package com.techgroup.techcop.security.jwt;

import com.techgroup.techcop.security.model.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String[] PUBLIC_AUTH_PREFIXES = {
            "/auth/login",
            "/auth/register",
            "/auth/registerRequest",
            "/auth/register/resend-code",
            "/auth/account-exists",
            "/auth/verify",
            "/auth/refresh",
            "/auth/logout",
            "/auth/google",
            "/auth/forgot-password",
            "/auth/password-recovery",
            "/auth/reset-password",
            "/auth/recaptcha",
            "/auth/google/client-config"
    };

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (isPublicAuthPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);
                    log.debug("JWT validado para {}", userDetails.getUsername());
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.debug("JWT invalido recibido para {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicAuthPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        for (String publicPrefix : PUBLIC_AUTH_PREFIXES) {
            if (path.equals(publicPrefix) || path.startsWith(publicPrefix + "/")) {
                return true;
            }
        }

        return false;
    }
}

