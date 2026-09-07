package com.mentorhub.repository;

import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * UserRepository - handles all database operations for the User entity.
 *
 * WHY THIS EXISTS:
 * Instead of writing SQL queries manually, Spring Data JPA lets us
 * define method names and it automatically generates the SQL.
 * JpaRepository gives us basic CRUD methods for free:
 * - save(user), findById(id), findAll(), deleteById(id), etc.
 *
 * HOW METHOD NAMES WORK:
 * "findByEmail" → Spring generates: SELECT * FROM users WHERE email = ?
 * "existsByEmail" → Spring generates: SELECT COUNT(*) FROM users WHERE email = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Used during login to look up the user.
     * Returns Optional<User> to safely handle the case where no user is found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with this email already exists.
     * Used during registration to prevent duplicate accounts.
     */
    boolean existsByEmail(String email);
}
