package com.mentorhub.controller;

import com.mentorhub.dto.AuthResponse;
import com.mentorhub.dto.LoginRequest;
import com.mentorhub.dto.RegisterRequest;
import com.mentorhub.dto.ResendVerificationRequest;
import com.mentorhub.dto.VerifyEmailRequest;
import com.mentorhub.service.AuthService;
import com.mentorhub.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @Autowired
    private EmailVerificationService emailVerificationService;

    /**
     * POST /api/auth/register
     *
     * WHY THIS API EXISTS:
     * New users (teachers or students) need an account to use MentR.
     * This endpoint creates the account and emails a verification link —
     * it no longer logs the user in immediately, since the account can't be
     * used until that link is clicked (see /login).
     *
     * @Valid triggers validation annotations in RegisterRequest
     * If validation fails, Spring automatically returns 400 Bad Request.
     *
     * @param request - contains name, email, password, role
     * @return a message telling the user to check their email
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * POST /api/auth/verify-email
     *
     * Called by the frontend's /verify-email page with the token from the
     * emailed link's query string. On success, the account can log in.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.getToken());
        return ResponseEntity.ok(Map.of("message", "Email verified! You can now sign in."));
    }

    /**
     * POST /api/auth/resend-verification
     *
     * Always returns the same message regardless of whether the email
     * exists, is already verified, or was rate-limited — see
     * EmailVerificationService.resend() for why.
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return ResponseEntity.ok(Map.of("message", emailVerificationService.resend(request.getEmail())));
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
