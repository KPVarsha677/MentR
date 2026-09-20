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
 * "findByEmailIgnoreCase" → Spring generates: SELECT * FROM users WHERE UPPER(email) = UPPER(?)
 * "existsByEmailIgnoreCase" → Spring generates: SELECT COUNT(*) FROM users WHERE UPPER(email) = UPPER(?)
 *
 * WHY IgnoreCase: MySQL's default collation made email lookups case-insensitive
 * for free ("Foo@x.com" matched "foo@x.com"). PostgreSQL's default comparison is
 * case-sensitive, which silently let case-variant duplicate accounts be created
 * and broke login for anyone who typed their email in different casing than they
 * registered with. IgnoreCase restores the original, expected behavior on both.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address, case-insensitively.
     * Used during login to look up the user.
     * Returns Optional<User> to safely handle the case where no user is found.
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if a user with this email already exists, case-insensitively.
     * Used during registration to prevent duplicate accounts.
     */
    boolean existsByEmailIgnoreCase(String email);
}
