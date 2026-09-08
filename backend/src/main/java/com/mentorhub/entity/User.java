package com.mentorhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * User Entity - represents a user account in the system.
 *
 * DATABASE TABLE: users
 */
@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    // False until the user clicks the emailed verification link. Login is
    // blocked while this is false (see AuthService.login).
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    // Profile picture filename stored under uploads/profile-pictures/
    private String profilePicture;

    // Short bio/about text editable from Edit Profile
    @Column(columnDefinition = "TEXT")
    private String bio;

    // Phone number
    private String phone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
