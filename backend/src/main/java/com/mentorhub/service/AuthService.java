package com.mentorhub.service;

import com.mentorhub.dto.AuthResponse;
import com.mentorhub.dto.LoginRequest;
import com.mentorhub.dto.RegisterRequest;
import com.mentorhub.entity.StudentProfile;
import com.mentorhub.entity.TeacherProfile;
import com.mentorhub.entity.User;
import com.mentorhub.exception.EmailNotVerifiedException;
import com.mentorhub.repository.StudentProfileRepository;
import com.mentorhub.repository.TeacherProfileRepository;
import com.mentorhub.repository.UserRepository;
import com.mentorhub.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuthService - handles user registration and login.
 *
 * WHY THIS SERVICE EXISTS:
 * Authentication is the first thing a user interacts with.
 * This service handles:
 * 1. Registration: create a new user account, hash the password, create a profile
 * 2. Login: verify credentials, generate and return a JWT token
 *
 * @Service tells Spring this is a service bean (business logic layer)
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailVerificationService emailVerificationService;

    // Shared secret that must be supplied to register as ROLE_TEACHER — set
    // via the TEACHER_INVITE_CODE environment variable (or the gitignored
    // backend/config/application.properties for local dev) and handed out by
    // the administrator to actual faculty. Without this, anyone visiting the
    // public registration page could pick "Teacher" and get full access to
    // every student's data and verification powers.
    @Value("${app.teacher-invite-code:}")
    private String teacherInviteCode;

    // ─────────────────────────────────────────────────────────────────────
    // Login brute-force protection.
    //
    // WHY THIS EXISTS:
    // Before this, there was no limit on login attempts — a script could try
    // unlimited passwords against any email. This is an in-memory, per-email
    // lockout: after MAX_ATTEMPTS wrong passwords in a row, that email is
    // locked out for LOCKOUT_MINUTES before another attempt is allowed. It
    // resets on a successful login. Being in-memory, it only protects a
    // single backend instance (fine for this deployment) and resets on
    // restart — acceptable for this app's scale, but not a substitute for a
    // shared store if this is ever run behind multiple instances.
    // ─────────────────────────────────────────────────────────────────────
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_MINUTES = 15;

    private static class LoginAttempts {
        int failedCount;
        LocalDateTime lockedUntil;
    }

    private final ConcurrentHashMap<String, LoginAttempts> loginAttempts = new ConcurrentHashMap<>();

    /**
     * Register a new user (teacher or student).
     *
     * STEP BY STEP:
     * 1. Check if email already exists (to prevent duplicates)
     * 2. Create a new User entity with hashed password, unverified
     * 3. Save the User to the database
     * 4. Create an empty profile (StudentProfile or TeacherProfile)
     * 5. Issue a verification token and email the link — no JWT is issued
     *    here anymore; the account can't log in until that link is clicked
     *    (see login() below)
     *
     * @param request - RegisterRequest DTO containing name, email, password, role
     * @return a message telling the user to check their email
     */
    public Map<String, String> register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists");
        }

        // Validate role
        if (!request.getRole().equals("ROLE_TEACHER") && !request.getRole().equals("ROLE_STUDENT")) {
            throw new RuntimeException("Invalid role. Must be ROLE_TEACHER or ROLE_STUDENT");
        }

        // Registering as a teacher requires the shared invite code — without
        // this check, anyone on the public registration page could pick
        // "Teacher" and get full access to every student's data.
        if (request.getRole().equals("ROLE_TEACHER")) {
            if (teacherInviteCode == null || teacherInviteCode.isBlank()) {
                throw new RuntimeException(
                        "Teacher registration is not configured. Contact your administrator.");
            }
            if (!constantTimeEquals(teacherInviteCode, request.getTeacherInviteCode())) {
                throw new RuntimeException("Invalid teacher invite code");
            }
        }

        // Create and save the User entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // BCrypt hash the password — NEVER store plain text passwords!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userRepository.save(user);

        // Create an empty profile based on the user's role
        // This way, the profile page will always have a record to work with
        if (request.getRole().equals("ROLE_STUDENT")) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(user);
            studentProfileRepository.save(profile);
        } else {
            TeacherProfile profile = new TeacherProfile();
            profile.setUser(user);
            teacherProfileRepository.save(profile);
        }

        // Send the verification email — login is blocked until this link is clicked
        emailVerificationService.issueAndSend(user);

        return Map.of("message",
                "Registration successful! Please check your email to verify your account before signing in.");
    }

    /**
     * Login an existing user.
     *
     * STEP BY STEP:
     * 1. Use Spring Security's AuthenticationManager to verify email+password
     * 2. If credentials are wrong, it throws an exception automatically
     * 3. Load the user from the database
     * 4. Reject if the account's email hasn't been verified yet
     * 5. Generate a JWT token and return it
     *
     * @param request - LoginRequest DTO with email and password
     * @return AuthResponse with JWT token and user information
     */
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        checkNotLockedOut(email);

        try {
            // This line does the actual authentication:
            // - Loads the user from database (via CustomUserDetailsService)
            // - Compares the provided password with the stored BCrypt hash
            // - Throws BadCredentialsException if credentials are wrong
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            recordFailedAttempt(email);
            throw ex;
        }

        // Authentication succeeded — clear any prior failed attempts for this email
        loginAttempts.remove(email);

        // Load the full user entity to get the role and name
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Correct password, but the account hasn't clicked its verification
        // link yet — this check does NOT count as a failed login attempt,
        // since the password itself was right.
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before logging in. Check your inbox for the verification link.");
        }

        // Generate a new JWT token for this session
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private void checkNotLockedOut(String email) {
        LoginAttempts attempts = loginAttempts.get(email);
        if (attempts != null && attempts.lockedUntil != null) {
            if (LocalDateTime.now().isBefore(attempts.lockedUntil)) {
                throw new RuntimeException(
                        "Too many failed login attempts. Please try again in a few minutes.");
            }
            // Lockout window has passed — reset and allow a fresh attempt.
            loginAttempts.remove(email);
        }
    }

    private void recordFailedAttempt(String email) {
        LoginAttempts attempts = loginAttempts.computeIfAbsent(email, k -> new LoginAttempts());
        synchronized (attempts) {
            attempts.failedCount++;
            if (attempts.failedCount >= MAX_LOGIN_ATTEMPTS) {
                attempts.lockedUntil = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
            }
        }
    }

    /**
     * Compares two strings without leaking timing information about where
     * they first differ — a plain String.equals() short-circuits on the
     * first mismatched character, which (in principle) lets an attacker
     * guess a secret one character at a time by measuring response time.
     */
    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
