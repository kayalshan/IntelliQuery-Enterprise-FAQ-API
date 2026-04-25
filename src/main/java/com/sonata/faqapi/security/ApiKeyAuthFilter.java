package com.sonata.faqapi.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private static final String API_KEY_HEADER = "X-API-Key";

//    @Value("${app.security.api-key:changeme-in-production}")
//    @Value("${APP_API_KEY:test-api-key}")
    @Value("${APP_API_KEY:changeme-dev}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (StringUtils.hasText(apiKey) && apiKey.equals(validApiKey)) {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "api-client",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_API_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Authenticated API request from {}", request.getRemoteAddr());
        } else {
            log.warn("Rejected request with invalid or missing API key from {}", request.getRemoteAddr());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}
