package com.mentorhub.service;

import com.mentorhub.entity.EmailVerificationToken;
import com.mentorhub.entity.User;
import com.mentorhub.repository.EmailVerificationTokenRepository;
import com.mentorhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EmailVerificationService - issues, emails, and checks email-verification
 * tokens, and gates login on the account being verified.
 *
 * SECURITY NOTES:
 * - The raw token is a 32-byte SecureRandom value (256 bits of entropy) —
 *   not guessable by brute force. Only its SHA-256 hash is persisted; see
 *   EmailVerificationToken for why a fast hash (not BCrypt) is correct here.
 * - Every response that might reveal whether an email is registered
 *   (resend) returns the exact same message and status regardless of the
 *   actual outcome, so an attacker can't enumerate valid accounts by
 *   watching for a difference.
 * - resend() is rate-limited per email address to stop someone from
 *   spamming a victim's inbox with verification emails.
 */
@Service
public class EmailVerificationService {

    private static final int TOKEN_BYTES = 32;
    private static final long EXPIRY_HOURS = 24;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private static final String GENERIC_RESEND_MESSAGE =
            "If an account with that email exists and isn't verified yet, a new verification link has been sent.";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final SecureRandom secureRandom = new SecureRandom();

    // In-memory per-email cooldown for resend requests. Single-instance only
    // (fine at this app's scale) — resets on restart.
    private final ConcurrentHashMap<String, LocalDateTime> lastResendAt = new ConcurrentHashMap<>();

    /**
     * Create a fresh token for this user, invalidate any previous unused
     * tokens (so only the newest link can ever work), and send it.
     */
    public void issueAndSend(User user) {
        invalidateExistingTokens(user);

        String rawToken = generateRawToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusHours(EXPIRY_HOURS));
        tokenRepository.save(token);

        String link = frontendUrl + "/verify-email?token=" + rawToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), link);
    }

    /**
     * Verify a raw token from the emailed link. Throws a generic message on
     * any failure (not found / expired / already used) so a guessed or
     * reused token doesn't tell an attacker which case they hit.
     */
    public void verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new RuntimeException("Invalid or expired verification link.");
        }

        EmailVerificationToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification link."));

        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired verification link.");
        }

        token.setUsed(true);
        tokenRepository.save(token);

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Resend a verification link. Always returns the same generic message
     * to the caller regardless of whether the email exists, is already
     * verified, or is rate-limited — only the internal behavior differs.
     */
    public String resend(String email) {
        String normalized = email.toLowerCase();

        LocalDateTime last = lastResendAt.get(normalized);
        boolean rateLimited = last != null &&
                last.plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now());

        if (!rateLimited) {
            userRepository.findByEmail(email)
                    .filter(user -> !user.isEmailVerified())
                    .ifPresent(user -> {
                        lastResendAt.put(normalized, LocalDateTime.now());
                        issueAndSend(user);
                    });
        }

        return GENERIC_RESEND_MESSAGE;
    }

    private void invalidateExistingTokens(User user) {
        var existing = tokenRepository.findByUserAndUsedFalse(user);
        existing.forEach(t -> t.setUsed(true));
        tokenRepository.saveAll(existing);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
