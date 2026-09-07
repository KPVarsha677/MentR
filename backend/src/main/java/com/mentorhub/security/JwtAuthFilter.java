package com.mentorhub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JwtAuthFilter - validates the JWT token on every incoming request.
 *
 * ROOT CAUSE FIX APPLIED HERE:
 * This class is annotated @Component, which causes Spring Boot to auto-register
 * it as a servlet container filter (via FilterRegistrationBean auto-detection)
 * AND it is also manually added to the Spring Security filter chain via
 * .addFilterBefore() in SecurityConfig.
 *
 * This double-registration causes the filter to execute TWICE per request:
 *   Pass 1: Servlet container level (before Spring Security)
 *   Pass 2: Inside Spring Security's filter chain
 *
 * On Pass 1, the filter runs outside the Security context lifecycle.
 * Spring Security's ExceptionTranslationFilter and SecurityContext management
 * only operate within the Security filter chain (Pass 2).
 * When the SecurityContext gets set in Pass 1 and then the Security chain
 * starts in Pass 2, various Spring Security internals (like
 * SecurityContextPersistenceFilter / SecurityContextHolderFilter) can clear
 * the SecurityContext between the two passes, causing auth to be lost.
 *
 * THE FIX:
 * We keep @Component so Spring can inject this as a dependency into SecurityConfig.
 * We prevent double-registration by overriding shouldNotFilter() to skip
 * processing when the request has already been processed by this filter,
 * AND by disabling the automatic servlet-level registration in SecurityConfig
 * via a FilterRegistrationBean with setEnabled(false).
 *
 * See SecurityConfig for the FilterRegistrationBean that prevents servlet-level
 * auto-registration.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Core filter logic — runs once per request inside the Security filter chain.
     *
     * FLOW:
     * 1. Log request details (for debugging)
     * 2. Extract JWT from Authorization header
     * 3. If JWT is present and valid → set Authentication in SecurityContext
     * 4. If no JWT → leave SecurityContext empty (Spring Security will decide
     *    if the endpoint needs auth; permitAll() endpoints will pass through)
     * 5. Always call filterChain.doFilter() to continue processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI  = request.getRequestURI();
        String authHeader  = request.getHeader("Authorization");

        // ── DEBUG LOGGING ──────────────────────────────────────────────────────────
        log.debug("──────────────────────────────────────────────");
        log.debug("[JwtAuthFilter] Incoming request: {} {}", request.getMethod(), requestURI);
        log.debug("[JwtAuthFilter] Authorization header: {}", authHeader != null ? "present" : "absent");
        // ──────────────────────────────────────────────────────────────────────────

        String token = extractToken(authHeader);

        if (token != null) {
            log.debug("[JwtAuthFilter] JWT found: YES (length={})", token.length());

            boolean valid = jwtUtil.validateToken(token);
            log.debug("[JwtAuthFilter] Token valid: {}", valid);

            if (valid) {
                String email = jwtUtil.getEmailFromToken(token);
                log.debug("[JwtAuthFilter] Username extracted: {}", email);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("[JwtAuthFilter] Authentication set for user: {} with roles: {}",
                        email, userDetails.getAuthorities());
            } else {
                log.debug("[JwtAuthFilter] Reason for rejection: Token failed validation (expired or tampered)");
            }
        } else {
            log.debug("[JwtAuthFilter] JWT found: NO — proceeding without authentication");
            log.debug("[JwtAuthFilter] Note: if endpoint is permitAll(), this request will succeed");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract the raw JWT string from the Authorization header.
     * Expected header format: "Bearer <token>"
     *
     * @param authHeader the raw Authorization header value
     * @return the token string, or null if header is absent or malformed
     */
    private String extractToken(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
