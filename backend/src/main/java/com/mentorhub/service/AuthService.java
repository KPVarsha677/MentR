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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    /**
     * Register a new user (teacher or student).
     *
     * STEP BY STEP:
     * 1. Check if email already exists (to prevent duplicates)
     * 2. Create a new User entity with hashed password
     * 3. Save the User to the database
     * 4. Create an empty profile (StudentProfile or TeacherProfile)
     * 5. Generate a JWT token and return it with user info
     *
     * @param request - RegisterRequest DTO containing name, email, password, role
     * @return AuthResponse with JWT token and user information
     */
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists");
        }

        // Validate role
        if (!request.getRole().equals("ROLE_TEACHER") && !request.getRole().equals("ROLE_STUDENT")) {
            throw new RuntimeException("Invalid role. Must be ROLE_TEACHER or ROLE_STUDENT");
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

        // Generate JWT token and return response
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
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
        // This line does the actual authentication:
        // - Loads the user from database (via CustomUserDetailsService)
        // - Compares the provided password with the stored BCrypt hash
        // - Throws BadCredentialsException if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // If we reach here, authentication succeeded
        // Load the full user entity to get the role and name
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a new JWT token for this session
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
