package com.mentorhub.security;

import com.mentorhub.entity.User;
import com.mentorhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService - loads user details from the database for Spring Security.
 *
 * WHY THIS EXISTS:
 * Spring Security doesn't know about our User entity or our database.
 * We need to tell Spring Security HOW to load a user when given a username (email).
 * By implementing UserDetailsService and overriding loadUserByUsername(),
 * we connect our database to Spring Security.
 *
 * HOW IT WORKS:
 * 1. During login, Spring Security calls loadUserByUsername(email)
 * 2. We look up the user in our database
 * 3. We return a UserDetails object that Spring Security understands
 * 4. Spring Security compares the password hash for authentication
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by email (we use email as the username).
     *
     * @param email - the email entered during login
     * @return UserDetails object containing user info Spring Security needs
     * @throws UsernameNotFoundException if no user with this email exists
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user in database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Wrap our User entity in a UserDetails that also carries the numeric
        // user ID, so controllers can verify the authenticated identity
        // instead of trusting a client-supplied id (see SecurityUtils).
        return new CustomUserDetails(user);
    }
}
