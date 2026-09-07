package com.mentorhub.config;

import com.mentorhub.security.JwtAuthFilter;
import com.mentorhub.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig — Spring Security configuration for MentR.
 *
 * ═══════════════════════════════════════════════════════════════
 * ROOT CAUSE FIXES APPLIED IN THIS CLASS (3 bugs fixed):
 * ═══════════════════════════════════════════════════════════════
 *
 * FIX 1 — Double-registration of JwtAuthFilter (PRIMARY 403 CAUSE)
 * ─────────────────────────────────────────────────────────────────
 * PROBLEM:
 *   JwtAuthFilter is annotated @Component. Spring Boot auto-registers
 *   ALL @Component beans that extend Filter into the servlet container's
 *   filter chain via FilterRegistrationBean auto-detection.
 *   SecurityConfig also calls .addFilterBefore(jwtAuthFilter, ...).
 *   Result: the filter runs TWICE per request — once in the servlet
 *   container chain and once inside the Spring Security filter chain.
 *
 *   The SecurityContextHolderFilter (Spring Security 6) saves and
 *   CLEARS the SecurityContext after the Security filter chain finishes.
 *   When the filter runs first at the servlet level (Pass 1), it sets
 *   authentication. Then the Security filter chain starts (Pass 2),
 *   SecurityContextHolderFilter creates a NEW empty SecurityContext,
 *   overwriting what Pass 1 set. Authentication is gone. → 403.
 *
 * FIX:
 *   Declare a FilterRegistrationBean<JwtAuthFilter> with setEnabled(false).
 *   This tells Spring Boot: "Do NOT auto-register this filter in the
 *   servlet container. I am managing it manually via SecurityConfig."
 *   The filter then runs ONLY inside the Spring Security chain — exactly once.
 *
 * FIX 2 — OPTIONS (CORS pre-flight) requests blocked with 403
 * ─────────────────────────────────────────────────────────────
 * PROBLEM:
 *   Browsers send an HTTP OPTIONS request before every cross-origin POST/PUT/DELETE.
 *   This OPTIONS "pre-flight" checks if the server allows the real request.
 *   The security config did not explicitly permit OPTIONS requests.
 *   Spring Security intercepted them → 403 → browser never sent the real request.
 *
 * FIX:
 *   Add .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() BEFORE other rules.
 *   This lets all pre-flight OPTIONS requests through without authentication.
 *
 * FIX 3 — DaoAuthenticationProvider not wired into AuthenticationManager
 * ─────────────────────────────────────────────────────────────────────────
 * PROBLEM:
 *   The authenticationProvider() bean was declared but Spring's
 *   AuthenticationConfiguration.getAuthenticationManager() builds its own
 *   provider from auto-config, ignoring our custom bean.
 *   Result: inconsistent UserDetailsService + PasswordEncoder wiring.
 *
 * FIX:
 *   Use HttpSecurity.authenticationProvider(authProvider) to explicitly register
 *   our DaoAuthenticationProvider into the HTTP security context, guaranteeing
 *   it is used for all authentication operations.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsRaw;

    // ─────────────────────────────────────────────────────────────────────────────
    // FIX 1: Disable servlet-container auto-registration of JwtAuthFilter.
    //
    // Spring Boot's FilterRegistrationBean mechanism sees any @Component bean
    // that extends javax/jakarta.servlet.Filter and registers it in Tomcat's
    // filter chain. We must disable this to prevent the filter from running
    // outside the Spring Security chain.
    //
    // By setting enabled = false, Spring Boot skips auto-registration.
    // The filter still gets added to the Security chain via .addFilterBefore()
    // in securityFilterChain() below — which is the only place it should run.
    // ─────────────────────────────────────────────────────────────────────────────
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        // CRITICAL: disable auto-registration in the servlet container.
        // The filter will ONLY run inside the Spring Security filter chain.
        registration.setEnabled(false);
        log.info("[SecurityConfig] JwtAuthFilter servlet-level auto-registration DISABLED. " +
                 "Filter will only run inside the Spring Security chain.");
        return registration;
    }

    /**
     * SecurityFilterChain — defines the complete security rules.
     *
     * RULE EVALUATION ORDER (top to bottom):
     * 1. OPTIONS requests → always permit (CORS pre-flight, FIX 2)
     * 2. /api/auth/**   → permit all (login, register — no JWT needed)
     * 3. /api/teacher/** → require ROLE_TEACHER
     * 4. /api/student/** → require ROLE_STUDENT
     * 5. Everything else → require any authenticated user
     *
     * IMPORTANT: requestMatchers() rules are evaluated in ORDER.
     * The FIRST matching rule wins.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Log configured public endpoints on startup — helps confirm configuration
        log.info("[SecurityConfig] Configuring security filter chain...");
        log.info("[SecurityConfig] Public endpoints: OPTIONS /**, /api/auth/**");
        log.info("[SecurityConfig] Teacher endpoints: /api/teacher/** (requires ROLE_TEACHER)");
        log.info("[SecurityConfig] Student endpoints: /api/student/** (requires ROLE_STUDENT)");
        log.info("[SecurityConfig] All other endpoints: require authentication");

        // FIX 3: Register our custom DaoAuthenticationProvider so Spring Security
        // uses OUR UserDetailsService and PasswordEncoder for authentication.
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        http
            // Disable CSRF — we use JWT (stateless), not cookies.
            // CSRF attacks only apply to cookie-based session authentication.
            .csrf(csrf -> csrf.disable())

            // Register our authentication provider (FIX 3)
            .authenticationProvider(authProvider)

            // Configure CORS using our corsConfigurationSource() bean
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth

                // FIX 2: Allow ALL OPTIONS requests without authentication.
                // This is required for CORS pre-flight to work correctly.
                // Browsers send OPTIONS before POST/PUT/DELETE cross-origin requests.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public endpoints — login and register require NO authentication
                .requestMatchers("/api/auth/**").permitAll()

                // Teacher-only endpoints
                // hasRole("TEACHER") automatically checks for "ROLE_TEACHER" authority.
                // Our DB stores "ROLE_TEACHER" and CustomUserDetailsService creates
                // new SimpleGrantedAuthority("ROLE_TEACHER") — this matches correctly.
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")

                // Student-only endpoints
                .requestMatchers("/api/student/**").hasRole("STUDENT")

                // All other endpoints require the user to be authenticated
                .anyRequest().authenticated()
            )

            // Stateless session — no server-side HTTP sessions.
            // Identity is carried entirely by the JWT token on each request.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Custom exception handlers — return clean JSON instead of HTML error pages.
            .exceptionHandling(ex -> ex
                // 401 Unauthorized: request has no token or invalid token on a protected route
                .authenticationEntryPoint((request, response, authException) -> {
                    log.warn("[SecurityConfig] 401 Unauthorized — no valid JWT for protected endpoint: {} {}",
                            request.getMethod(), request.getRequestURI());
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(
                        "{\"error\": \"Unauthorized — please login to access this resource\"}"
                    );
                })
                // 403 Forbidden: user is authenticated but lacks the required role
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.warn("[SecurityConfig] 403 Forbidden — insufficient role for endpoint: {} {}",
                            request.getMethod(), request.getRequestURI());
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write(
                        "{\"error\": \"Forbidden — you do not have permission to access this resource\"}"
                    );
                })
            )

            // Add our JWT filter into the Security filter chain — BEFORE the default
            // UsernamePasswordAuthenticationFilter. It will run ONLY here (not at
            // servlet container level — see jwtFilterRegistration() above).
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("[SecurityConfig] Security filter chain configured successfully.");
        return http.build();
    }

    /**
     * CORS configuration — allows the React frontend to call the backend API.
     *
     * WHY allowedOrigins uses a list (not setAllowedOriginPatterns):
     * setAllowedOrigins() with exact origins + setAllowCredentials(true) is valid
     * as long as the origin is an exact match (not a wildcard).
     * http://localhost:3000 is an exact origin → this works correctly.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Support a comma-separated list of origins (e.g. port 3000 and 3001)
        List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        configuration.setAllowedOrigins(origins);

        // Allow all standard HTTP methods + OPTIONS (for pre-flight)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow Authorization header (for JWT) and Content-Type (for JSON body)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        // Expose Authorization header so the frontend can read it if needed
        configuration.setExposedHeaders(List.of("Authorization"));

        // Allow credentials (cookies, Authorization header) to be sent
        configuration.setAllowCredentials(true);

        // Cache the pre-flight response for 1 hour (reduces OPTIONS requests)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("[SecurityConfig] CORS configured for origins: {}", origins);
        return source;
    }

    /**
     * BCryptPasswordEncoder bean.
     * All passwords are hashed with BCrypt before storing in the database.
     * BCrypt is one-way — you can verify but not reverse.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager bean.
     * Used by AuthService.login() to verify email + password during login.
     * Spring's AuthenticationConfiguration builds this using the registered
     * DaoAuthenticationProvider (which we wire in securityFilterChain via
     * http.authenticationProvider()).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
