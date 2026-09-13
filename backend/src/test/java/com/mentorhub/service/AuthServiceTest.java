package com.mentorhub.service;

import com.mentorhub.dto.AuthResponse;
import com.mentorhub.dto.LoginRequest;
import com.mentorhub.dto.RegisterRequest;
import com.mentorhub.entity.User;
import com.mentorhub.exception.EmailNotVerifiedException;
import com.mentorhub.repository.StudentProfileRepository;
import com.mentorhub.repository.TeacherProfileRepository;
import com.mentorhub.repository.UserRepository;
import com.mentorhub.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentProfileRepository studentProfileRepository;
    @Mock private TeacherProfileRepository teacherProfileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private EmailVerificationService emailVerificationService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        ReflectionTestUtils.setField(authService, "studentProfileRepository", studentProfileRepository);
        ReflectionTestUtils.setField(authService, "teacherProfileRepository", teacherProfileRepository);
        ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(authService, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(authService, "authenticationManager", authenticationManager);
        ReflectionTestUtils.setField(authService, "emailVerificationService", emailVerificationService);
        ReflectionTestUtils.setField(authService, "approvedTeacherEmails", Set.of("teacher@college.ac.in"));
    }

    private RegisterRequest studentRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@test.com");
        req.setPassword("password123");
        req.setRole("ROLE_STUDENT");
        return req;
    }

    // ── register ─────────────────────────────────────────────────────────

    private static final String TEST_IP = "127.0.0.1";

    @Test
    void register_newStudent_createsUnverifiedUserAndSendsVerificationEmail_noTokenReturned() {
        RegisterRequest req = studentRequest();
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        Map<String, String> response = authService.register(req, TEST_IP);

        verify(userRepository).save(argThat(u -> u.getEmail().equals("alice@test.com")
                && u.getPassword().equals("hashed")
                && !u.isEmailVerified()));
        verify(studentProfileRepository).save(any());
        verify(emailVerificationService).issueAndSend(any(User.class));
        verify(jwtUtil, never()).generateToken(anyString());
        assertTrue(response.get("message").toLowerCase().contains("check your email"));
    }

    @Test
    void register_duplicateEmail_isRejected() {
        RegisterRequest req = studentRequest();
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(req, TEST_IP));
        verify(emailVerificationService, never()).issueAndSend(any());
    }

    @Test
    void register_teacherEmailNotOnApprovedList_isRejected() {
        RegisterRequest req = studentRequest();
        req.setEmail("stranger@test.com");
        req.setRole("ROLE_TEACHER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req, TEST_IP));
        assertTrue(ex.getMessage().toLowerCase().contains("approved faculty list"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_teacherEmailOnApprovedList_succeeds() {
        RegisterRequest req = studentRequest();
        req.setEmail("teacher@college.ac.in");
        req.setRole("ROLE_TEACHER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        authService.register(req, TEST_IP);

        verify(userRepository).save(any(User.class));
        verify(teacherProfileRepository).save(any());
        verify(emailVerificationService).issueAndSend(any(User.class));
    }

    @Test
    void register_teacherEmailMatching_isCaseInsensitiveAndTrimsWhitespace() {
        RegisterRequest req = studentRequest();
        // Approved list holds "teacher@college.ac.in" (see setUp) — this
        // should still match despite different case and stray whitespace.
        req.setEmail("  TEACHER@College.AC.in  ");
        req.setRole("ROLE_TEACHER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        authService.register(req, TEST_IP);

        verify(userRepository).save(any(User.class));
        verify(emailVerificationService).issueAndSend(any(User.class));
    }

    @Test
    void register_teacherRegistrationNotConfigured_isRejectedWhenApprovedListIsEmpty() {
        ReflectionTestUtils.setField(authService, "approvedTeacherEmails", Set.<String>of());
        RegisterRequest req = studentRequest();
        req.setEmail("teacher@college.ac.in");
        req.setRole("ROLE_TEACHER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req, TEST_IP));
        assertTrue(ex.getMessage().toLowerCase().contains("not configured"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void parseApprovedTeacherEmails_lowercasesTrimsAndIgnoresBlankEntries() {
        ReflectionTestUtils.setField(authService, "approvedTeacherEmailsRaw",
                "  Teacher1@College.AC.IN , teacher2@college.ac.in ,, ");

        ReflectionTestUtils.invokeMethod(authService, "parseApprovedTeacherEmails");

        Object parsed = ReflectionTestUtils.getField(authService, "approvedTeacherEmails");
        assertEquals(Set.of("teacher1@college.ac.in", "teacher2@college.ac.in"), parsed);
    }

    @Test
    void parseApprovedTeacherEmails_supportsAnyNumberOfAddresses() {
        String manyEmails = "t1@college.ac.in,t2@college.ac.in,t3@college.ac.in,t4@college.ac.in,t5@college.ac.in";
        ReflectionTestUtils.setField(authService, "approvedTeacherEmailsRaw", manyEmails);

        ReflectionTestUtils.invokeMethod(authService, "parseApprovedTeacherEmails");

        Object parsed = ReflectionTestUtils.getField(authService, "approvedTeacherEmails");
        assertEquals(5, ((Set<?>) parsed).size());
    }

    // ── register: rate limiting ──────────────────────────────────────────

    @Test
    void register_tenAttemptsFromSameIpSucceed_eleventhIsRateLimited() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        for (int i = 0; i < 10; i++) {
            RegisterRequest req = studentRequest();
            req.setEmail("student" + i + "@test.com");
            authService.register(req, "1.2.3.4");
        }
        verify(userRepository, times(10)).save(any(User.class));

        RegisterRequest eleventh = studentRequest();
        eleventh.setEmail("student10@test.com");
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(eleventh, "1.2.3.4"));
        assertTrue(ex.getMessage().toLowerCase().contains("too many"));
        // The blocked 11th attempt must never reach the database.
        verify(userRepository, times(10)).save(any(User.class));
    }

    @Test
    void register_rateLimitIsPerIp_differentIpIsUnaffected() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        for (int i = 0; i < 10; i++) {
            RegisterRequest req = studentRequest();
            req.setEmail("busy" + i + "@test.com");
            authService.register(req, "5.5.5.5");
        }
        assertThrows(RuntimeException.class,
                () -> authService.register(studentRequest(), "5.5.5.5"));

        // A different IP has made no attempts yet and must not be blocked
        // just because "5.5.5.5" hit its limit.
        RegisterRequest fromOtherIp = studentRequest();
        fromOtherIp.setEmail("other@test.com");
        authService.register(fromOtherIp, "9.9.9.9");
        verify(userRepository).save(argThat(u -> u.getEmail().equals("other@test.com")));
    }

    @Test
    void register_rateLimitWindowExpires_resetsCount() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        for (int i = 0; i < 10; i++) {
            RegisterRequest req = studentRequest();
            req.setEmail("win" + i + "@test.com");
            authService.register(req, "8.8.8.8");
        }
        assertThrows(RuntimeException.class,
                () -> authService.register(studentRequest(), "8.8.8.8"));

        // Simulate the 60-minute window having elapsed for this IP by
        // reaching into the private per-IP bucket and backdating it.
        Map<?, ?> buckets = (Map<?, ?>) ReflectionTestUtils.getField(authService, "registrationAttempts");
        Object bucket = buckets.get("8.8.8.8");
        ReflectionTestUtils.setField(bucket, "windowStart", LocalDateTime.now().minusMinutes(61));

        RegisterRequest afterExpiry = studentRequest();
        afterExpiry.setEmail("afterExpiry@test.com");
        authService.register(afterExpiry, "8.8.8.8");
        verify(userRepository).save(argThat(u -> u.getEmail().equals("afterExpiry@test.com")));
    }

    @Test
    void register_unresolvableClientIp_isNotRateLimited() {
        // A null clientIp (e.g. IP genuinely couldn't be resolved) fails
        // open rather than sharing one bucket across every such caller.
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        for (int i = 0; i < 15; i++) {
            RegisterRequest req = studentRequest();
            req.setEmail("noip" + i + "@test.com");
            authService.register(req, null);
        }
        verify(userRepository, times(15)).save(any(User.class));
    }

    // ── login ────────────────────────────────────────────────────────────

    private User verifiedUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@test.com");
        user.setRole("ROLE_STUDENT");
        user.setEmailVerified(true);
        return user;
    }

    @Test
    void login_correctCredentialsButUnverifiedEmail_isBlocked() {
        User user = verifiedUser();
        user.setEmailVerified(false);
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("alice@test.com");
        req.setPassword("password123");

        assertThrows(EmailNotVerifiedException.class, () -> authService.login(req));
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void login_correctCredentialsAndVerifiedEmail_succeeds() {
        User user = verifiedUser();
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("alice@test.com")).thenReturn("signed-jwt");

        LoginRequest req = new LoginRequest();
        req.setEmail("alice@test.com");
        req.setPassword("password123");

        AuthResponse response = authService.login(req);

        assertEquals("signed-jwt", response.getToken());
        assertEquals("ROLE_STUDENT", response.getRole());
    }

    @Test
    void login_wrongPassword_throwsBadCredentials_andDoesNotCountEmailNotVerifiedAsAFailure() {
        // A wrong password must raise BadCredentialsException (counted toward
        // lockout); an unverified email must NOT be treated the same way,
        // since the password itself was correct in that case.
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        LoginRequest req = new LoginRequest();
        req.setEmail("alice@test.com");
        req.setPassword("wrong");

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_fiveWrongPasswordsThenLockedOut_evenWithCorrectPasswordAfterward() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        LoginRequest req = new LoginRequest();
        req.setEmail("locked@test.com");
        req.setPassword("wrong");

        for (int i = 0; i < 5; i++) {
            assertThrows(BadCredentialsException.class, () -> authService.login(req));
        }

        // 6th attempt (even hypothetically with the right password) is
        // blocked by the lockout before authenticationManager is even asked.
        reset(authenticationManager);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().toLowerCase().contains("too many"));
        verify(authenticationManager, never()).authenticate(any());
    }
}
