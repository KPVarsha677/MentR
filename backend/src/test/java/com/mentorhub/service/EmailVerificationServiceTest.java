package com.mentorhub.service;

import com.mentorhub.entity.EmailVerificationToken;
import com.mentorhub.entity.User;
import com.mentorhub.repository.EmailVerificationTokenRepository;
import com.mentorhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService();
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "tokenRepository", tokenRepository);
        ReflectionTestUtils.setField(service, "emailService", emailService);
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:3000");
    }

    private User newUser(String email, boolean verified) {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail(email);
        user.setEmailVerified(verified);
        return user;
    }

    // ── issueAndSend ────────────────────────────────────────────────────

    @Test
    void issueAndSend_savesUnusedTokenAndEmailsTheLink() {
        User user = newUser("student@test.com", false);
        when(tokenRepository.findByUserAndUsedFalse(user)).thenReturn(List.of());

        service.issueAndSend(user);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        EmailVerificationToken saved = captor.getValue();
        assertFalse(saved.isUsed());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now().plusHours(23)));
        assertTrue(saved.getExpiresAt().isBefore(LocalDateTime.now().plusHours(25)));

        verify(emailService).sendVerificationEmail(
                eq("student@test.com"), eq("Test User"), contains("http://localhost:3000/verify-email?token="));
    }

    @Test
    void issueAndSend_invalidatesAnyPreviousUnusedTokens() {
        User user = newUser("student@test.com", false);
        EmailVerificationToken oldToken = new EmailVerificationToken();
        oldToken.setUsed(false);
        when(tokenRepository.findByUserAndUsedFalse(user)).thenReturn(List.of(oldToken));

        service.issueAndSend(user);

        assertTrue(oldToken.isUsed(), "previous token should be marked used so it can't be replayed");
        verify(tokenRepository).saveAll(List.of(oldToken));
    }

    // ── verify ───────────────────────────────────────────────────────────

    @Test
    void verify_validToken_marksUsedAndVerifiesUser() {
        User user = newUser("student@test.com", false);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        service.verify("some-raw-token");

        assertTrue(token.isUsed());
        assertTrue(user.isEmailVerified());
        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    void verify_expiredToken_isRejectedWithGenericMessage() {
        User user = newUser("student@test.com", false);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // already expired
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.verify("expired-token"));
        assertEquals("Invalid or expired verification link.", ex.getMessage());
        assertFalse(user.isEmailVerified(), "an expired token must not verify the account");
    }

    @Test
    void verify_alreadyUsedToken_isRejectedWithSameGenericMessage() {
        User user = newUser("student@test.com", false);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setUsed(true); // already consumed once
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.verify("reused-token"));
        assertEquals("Invalid or expired verification link.", ex.getMessage());
    }

    @Test
    void verify_unknownToken_isRejectedWithSameGenericMessage() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.verify("made-up-token"));
        assertEquals("Invalid or expired verification link.", ex.getMessage());
    }

    // ── resend ───────────────────────────────────────────────────────────

    @Test
    void resend_unverifiedExistingAccount_sendsAndReturnsGenericMessage() {
        User user = newUser("student@test.com", false);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserAndUsedFalse(user)).thenReturn(List.of());

        String message = service.resend("student@test.com");

        verify(emailService).sendVerificationEmail(eq("student@test.com"), any(), any());
        assertTrue(message.toLowerCase().contains("if an account"));
    }

    @Test
    void resend_unknownEmail_returnsSameGenericMessageWithoutSending() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        String message = service.resend("nobody@test.com");

        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
        assertEquals(service.resend("nobody@test.com"), message, "message must be identical regardless of outcome");
    }

    @Test
    void resend_alreadyVerifiedAccount_returnsSameGenericMessageWithoutSending() {
        User user = newUser("verified@test.com", true);
        when(userRepository.findByEmail("verified@test.com")).thenReturn(Optional.of(user));

        String message = service.resend("verified@test.com");

        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
        assertNotNull(message);
    }

    @Test
    void resend_secondCallWithinCooldown_isRateLimitedButStillReturnsGenericMessage() {
        User user = newUser("student@test.com", false);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserAndUsedFalse(user)).thenReturn(List.of());

        service.resend("student@test.com");
        service.resend("student@test.com");

        // Only the first call should have actually triggered an email.
        verify(emailService, times(1)).sendVerificationEmail(any(), any(), any());
    }
}
