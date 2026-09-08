package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * EmailVerificationToken - a single-use, expiring token proving a user
 * clicked the link sent to their own email address.
 *
 * WHY WE STORE A HASH, NOT THE RAW TOKEN:
 * The raw token is emailed to the user and is effectively a bearer
 * credential — anyone who has it can verify that account. If the database
 * were ever read (backup leak, SQL injection, etc.), storing only a SHA-256
 * hash means the attacker still can't produce a token that hashes to a
 * matching row, the same reasoning as hashing passwords. Unlike passwords,
 * the raw value here is a high-entropy random token rather than something a
 * human chose, so a fast cryptographic hash (not BCrypt) is appropriate —
 * there's no low-entropy value for an attacker to dictionary-guess.
 *
 * DATABASE TABLE: email_verification_tokens
 */
@Entity
@Table(name = "email_verification_tokens")
@Data
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Set true the moment the token is successfully used, so it can never
    // be replayed even if it hasn't expired yet.
    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
