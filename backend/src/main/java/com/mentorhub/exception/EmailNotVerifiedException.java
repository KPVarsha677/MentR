package com.mentorhub.exception;

/**
 * Thrown when a user with correct credentials tries to log in before
 * clicking their emailed verification link. Kept distinct from
 * BadCredentialsException/AccessDeniedException so GlobalExceptionHandler
 * can return a machine-readable code the frontend uses to show a
 * "resend verification email" action instead of a generic error.
 */
public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
