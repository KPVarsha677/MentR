package com.mentorhub.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityUtils - resolves the real, token-verified identity of the caller.
 *
 * WHY THIS EXISTS:
 * Every "self" endpoint in this app (a student's own profile, a teacher's
 * own classroom, etc.) takes the target user's id as a path or query
 * parameter. Several controllers used to trust that parameter directly —
 * e.g. GET /api/student/{userId}/profile just loaded whatever userId was in
 * the URL, and the update/delete ownership checks compared an entity's owner
 * against that same client-supplied userId. That means the "ownership check"
 * never actually verified who was logged in; it only checked that the URL's
 * id matched the URL's id. Any authenticated student could read or modify
 * any other student's data by changing the id in the URL.
 *
 * requireSelf(id) closes that hole: it compares the requested id against the
 * id embedded in the caller's verified JWT (via CustomUserDetails), which
 * cannot be spoofed by the client, and rejects the request with 403 if they
 * don't match.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * @return the numeric user id of the currently authenticated caller.
     */
    public static Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new AccessDeniedException("No authenticated user found");
    }

    /**
     * Verify that the given id belongs to the currently authenticated caller.
     * Throws AccessDeniedException (mapped to 403 by GlobalExceptionHandler)
     * if it does not — e.g. a student passing another student's id.
     */
    public static void requireSelf(Long id) {
        if (id == null || !id.equals(getCurrentUserId())) {
            throw new AccessDeniedException("You are not authorized to access this resource");
        }
    }
}
