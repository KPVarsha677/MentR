package com.mentorhub.controller;

import com.mentorhub.dto.AuthResponse;
import com.mentorhub.dto.LoginRequest;
import com.mentorhub.dto.RegisterRequest;
import com.mentorhub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - handles user registration and login API endpoints.
 *
 * WHY THIS EXISTS:
 * This is the entry point for authentication.
 * It receives HTTP requests from the React frontend,
 * delegates to AuthService for business logic,
 * and returns responses.
 *
 * API ENDPOINTS:
 * POST /api/auth/register → creates a new account
 * POST /api/auth/login    → authenticates and returns a JWT
 *
 * @RestController = @Controller + @ResponseBody
 * This means every method returns JSON (not HTML pages)
 *
 * @RequestMapping("/api/auth") - all endpoints start with /api/auth
 * @CrossOrigin - allows requests from the MentR React frontend
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /api/auth/register
     *
     * WHY THIS API EXISTS:
     * New users (teachers or students) need an account to use MentR.
     * This endpoint creates the account.
     *
     * @Valid triggers validation annotations in RegisterRequest
     * If validation fails, Spring automatically returns 400 Bad Request.
     *
     * @param request - contains name, email, password, role
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/login
     *
     * WHY THIS API EXISTS:
     * Existing users need to log in to get a JWT token.
     * The React frontend stores this token and sends it with every future request.
     *
     * @param request - contains email and password
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
