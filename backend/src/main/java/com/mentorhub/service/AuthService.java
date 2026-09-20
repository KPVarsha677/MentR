package com.mentorhub.service;

import com.mentorhub.dto.AuthResponse;
import com.mentorhub.dto.LoginRequest;
import com.mentorhub.dto.RegisterRequest;
import com.mentorhub.entity.StudentProfile;
import com.mentorhub.entity.TeacherProfile;
import com.mentorhub.entity.User;
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

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    // Comma-separated allowlist of faculty email addresses permitted to
    // register as ROLE_TEACHER — set via the APPROVED_TEACHER_EMAILS
    // environment variable (or the gitignored backend/config/application.properties
    // for local dev). Without this, anyone visiting the public registration
    // page could pick "Teacher" and get full access to every student's data
    // and verification powers. The department adds/removes faculty by
    // editing this one comma-separated value — no code change needed.
    @Value("${app.approved-teacher-emails:}")
    private String approvedTeacherEmailsRaw;

    // Same allowlist mechanism, for ROLE_STUDENT registration — set via
    // APPROVED_STUDENT_EMAILS. Kept as a separate list from the teacher one
    // so each can be managed independently.
    @Value("${app.approved-student-emails:}")
    private String approvedStudentEmailsRaw;

    // Parsed once at startup into lowercased, trimmed sets for O(1),
    // case-insensitive lookups — supports any number of addresses.
    private Set<String> approvedTeacherEmails = Collections.emptySet();
    private Set<String> approvedStudentEmails = Collections.emptySet();

    @PostConstruct
    private void parseApprovedEmailAllowlists() {
        approvedTeacherEmails = parseAllowlist(approvedTeacherEmailsRaw);
        approvedStudentEmails = parseAllowlist(approvedStudentEmailsRaw);
    }

    private Set<String> parseAllowlist(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptySet();
        }
        Set<String> parsed = new HashSet<>();
        for (String email : raw.split(",")) {
            String normalized = email.trim().toLowerCase();
            if (!normalized.isEmpty()) {
                parsed.add(normalized);
            }
        }
        return parsed;
    }

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

    // ─────────────────────────────────────────────────────────────────────
    // Registration abuse protection.
    //
    // WHY THIS EXISTS:
    // Before this, POST /register had no limit at all — a script could mass-
    // create accounts from one IP, each one triggering a real verification
    // email send (burning SMTP quota/reputation) and filling the DB with
    // junk unverified users. This is an in-memory, per-IP fixed-window
    // counter: once an IP makes MAX_REGISTRATIONS_PER_IP attempts within
    // REGISTRATION_WINDOW_MINUTES, further attempts are rejected until the
    // window (measured from that IP's first attempt in the window) elapses,
    // at which point the count resets. Same in-memory/single-instance
    // caveat as the login lockout above.
    // ─────────────────────────────────────────────────────────────────────
    private static final int MAX_REGISTRATIONS_PER_IP = 10;
    private static final long REGISTRATION_WINDOW_MINUTES = 60;

    private static class RegistrationAttempts {
        int count;
        LocalDateTime windowStart;
    }

    private final ConcurrentHashMap<String, RegistrationAttempts> registrationAttempts = new ConcurrentHashMap<>();

    /**
     * Register a new user (teacher or student).
     *
     * STEP BY STEP:
     * 1. Enforce the per-IP registration rate limit
     * 2. Check if email already exists (to prevent duplicates)
     * 3. Create a new User entity with hashed password
     * 4. Save the User to the database
     * 5. Create an empty profile (StudentProfile or TeacherProfile)
     *
     * @param request  - RegisterRequest DTO containing name, email, password, role
     * @param clientIp - the caller's real IP (see ClientIpResolver), used only
     *                   for rate limiting; may be null if truly unknown, in
     *                   which case this call is not rate limited (fail open
     *                   rather than lock every unresolvable-IP caller
     *                   together under one shared bucket)
     * @return a message confirming the account was created
     */
    public Map<String, String> register(RegisterRequest request, String clientIp) {
        enforceRegistrationRateLimit(clientIp);

        // Check for duplicate email (case-insensitive — see UserRepository)
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists");
        }

        // Validate role
        if (!request.getRole().equals("ROLE_TEACHER") && !request.getRole().equals("ROLE_STUDENT")) {
            throw new RuntimeException("Invalid role. Must be ROLE_TEACHER or ROLE_STUDENT");
        }

        // Registering as a teacher requires the email to be on the approved
        // faculty allowlist — without this check, anyone on the public
        // registration page could pick "Teacher" and get full access to
        // every student's data.
        if (request.getRole().equals("ROLE_TEACHER")) {
            if (approvedTeacherEmails.isEmpty()) {
                throw new RuntimeException(
                        "Teacher registration is not configured. Contact your administrator.");
            }
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            if (!approvedTeacherEmails.contains(normalizedEmail)) {
                throw new RuntimeException(
                        "This email is not on the approved faculty list. Contact your administrator.");
            }
        }

        // Same allowlist gate for students — an empty/unset list fails
        // closed (rejects everyone) rather than failing open, matching the
        // teacher allowlist's behavior.
        if (request.getRole().equals("ROLE_STUDENT")) {
            if (approvedStudentEmails.isEmpty()) {
                throw new RuntimeException(
                        "Student registration is not configured. Contact your administrator.");
            }
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            if (!approvedStudentEmails.contains(normalizedEmail)) {
                throw new RuntimeException(
                        "This email is not on the approved student list. Contact your administrator.");
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

        return Map.of("message", "Registration successful! You can now sign in.");
    }

    /**
     * Login an existing user.
     *
     * STEP BY STEP:
     * 1. Use Spring Security's AuthenticationManager to verify email+password
     * 2. If credentials are wrong, it throws an exception automatically
     * 3. Load the user from the database
     * 4. Generate a JWT token and return it
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

        // Load the full user entity to get the role and name (case-insensitive
        // — must match however CustomUserDetailsService just found this user)
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

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
     * Reject registration once an IP has made MAX_REGISTRATIONS_PER_IP
     * attempts within the current REGISTRATION_WINDOW_MINUTES window;
     * otherwise record this attempt. The window is fixed, not sliding: it
     * starts on that IP's first attempt and resets entirely once it elapses.
     */
    private void enforceRegistrationRateLimit(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            // No identifiable client IP — fail open rather than share one
            // bucket across every caller we can't distinguish.
            return;
        }
        RegistrationAttempts attempts = registrationAttempts.computeIfAbsent(clientIp, k -> new RegistrationAttempts());
        synchronized (attempts) {
            LocalDateTime now = LocalDateTime.now();
            boolean windowExpired = attempts.windowStart == null
                    || now.isAfter(attempts.windowStart.plusMinutes(REGISTRATION_WINDOW_MINUTES));
            if (windowExpired) {
                attempts.windowStart = now;
                attempts.count = 0;
            }
            if (attempts.count >= MAX_REGISTRATIONS_PER_IP) {
                throw new RuntimeException(
                        "Too many registration attempts from this network. Please try again later.");
            }
            attempts.count++;
        }
    }

}
