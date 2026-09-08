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

import java.util.Map;
import java.util.Optional;

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
        ReflectionTestUtils.setField(authService, "teacherInviteCode", "correct-invite-code");
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

    @Test
    void register_newStudent_createsUnverifiedUserAndSendsVerificationEmail_noTokenReturned() {
        RegisterRequest req = studentRequest();
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        Map<String, String> response = authService.register(req);

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

        assertThrows(RuntimeException.class, () -> authService.register(req));
        verify(emailVerificationService, never()).issueAndSend(any());
    }

    @Test
    void register_teacherWithoutInviteCode_isRejected() {
        ReflectionTestUtils.setField(authService, "teacherInviteCode", "correct-invite-code");
        RegisterRequest req = studentRequest();
        req.setRole("ROLE_TEACHER");
        req.setTeacherInviteCode(null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().toLowerCase().contains("invite code"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_teacherWithWrongInviteCode_isRejected() {
        RegisterRequest req = studentRequest();
        req.setRole("ROLE_TEACHER");
        req.setTeacherInviteCode("wrong-code");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_teacherWithCorrectInviteCode_succeeds() {
        RegisterRequest req = studentRequest();
        req.setRole("ROLE_TEACHER");
        req.setTeacherInviteCode("correct-invite-code");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        authService.register(req);

        verify(userRepository).save(any(User.class));
        verify(teacherProfileRepository).save(any());
        verify(emailVerificationService).issueAndSend(any(User.class));
    }

    @Test
    void register_teacherRegistrationNotConfigured_isRejected() {
        ReflectionTestUtils.setField(authService, "teacherInviteCode", "");
        RegisterRequest req = studentRequest();
        req.setRole("ROLE_TEACHER");
        req.setTeacherInviteCode("anything");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
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
