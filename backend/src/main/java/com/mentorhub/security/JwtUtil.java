package com.mentorhub.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * JwtUtil - A utility class for creating and validating JWT tokens.
 *
 * WHAT IS A JWT?
 * JWT = JSON Web Token.
 * It is a compact string that contains user information (like userId, email, role)
 * encoded and digitally signed.
 *
 * Example JWT: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.SIGNATURE
 * Structure: [HEADER].[PAYLOAD].[SIGNATURE]
 *
 * WHY USE JWT?
 * Traditional sessions store user data on the SERVER.
 * JWT stores user data IN THE TOKEN itself.
 * The server just validates the signature — no session storage needed.
 * This makes our backend stateless and simple.
 *
 * HOW IT WORKS IN THIS APP:
 * 1. User logs in → server creates a JWT with userId, email, role
 * 2. Server sends JWT back to the client (React frontend)
 * 3. React stores the JWT in localStorage
 * 4. Every API request includes JWT in the Authorization header
 * 5. Server validates the JWT and identifies the user
 */
@Component
public class JwtUtil {

    // The secret key used to sign and verify tokens (from application.properties)
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // How long a token stays valid (from application.properties, default 24 hours)
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate a JWT token for a user.
     * Called after successful login.
     *
     * @param email - the user's email (stored as the subject of the token)
     * @return the JWT token string
     */
    public String generateToken(String email) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(email)                                    // Who this token is for
                .setIssuedAt(new Date())                              // When it was created
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // When it expires
                .signWith(key, SignatureAlgorithm.HS512)              // Sign it with our secret key
                .compact();                                           // Build and return the string
    }

    /**
     * Extract the email (subject) from a JWT token.
     * Used to identify which user is making a request.
     *
     * @param token - the JWT token string
     * @return email of the user
     */
    public String getEmailFromToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validate a JWT token.
     * Checks if the token is properly signed and not expired.
     *
     * @param token - the JWT token to validate
     * @return true if valid, false if invalid or expired
     */
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid (wrong signature, expired, malformed, etc.)
            return false;
        }
    }
}
