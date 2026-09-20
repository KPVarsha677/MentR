package com.mentorhub.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - catches exceptions thrown anywhere in the app and
 * returns clean JSON error responses to the frontend.
 *
 * WHY THE ORDER OF @ExceptionHandler MATTERS:
 * Spring picks the MOST SPECIFIC handler that matches the thrown exception.
 * AccessDeniedException and BadCredentialsException must be declared BEFORE
 * the generic RuntimeException handler — otherwise the generic handler would
 * catch them first and return a misleading 400 instead of 403/401.
 *
 * FIXED BUGS:
 * 1. AccessDeniedException was being caught by handleRuntimeException → returned 400
 *    (Spring Security's own 403 handler never ran).
 *    Fix: explicit @ExceptionHandler(AccessDeniedException.class) → 403.
 *
 * 2. MissingServletRequestParameterException (missing @RequestParam) returned a
 *    confusing 400 {"error": "Required request parameter ... is not present"}.
 *    Fix: explicit handler with a clear, field-level error message.
 *
 * 3. MethodArgumentTypeMismatchException (e.g., teacherId="null" can't be parsed
 *    as Long) returned a raw Spring message. Fix: explicit handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Spring Security access denied (403).
     *
     * WHY THIS MUST COME BEFORE handleRuntimeException:
     * AccessDeniedException extends RuntimeException.
     * Without this handler, the generic RuntimeException handler below would
     * catch it and return 400 — hiding the real security failure from the client.
     *
     * WHEN THIS TRIGGERS:
     * A student tries to call a teacher-only endpoint (or vice-versa).
     * The @PreAuthorize check fails → Spring throws AccessDeniedException.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Access denied. You do not have permission to perform this action.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle login failures (wrong password or email) → 400 Bad Request.
     *
     * WHY 400 AND NOT 401:
     * The Axios interceptor in api.js treats every 401 as an expired-session event
     * and immediately clears localStorage + redirects to /login. If we return 401
     * here the error message ("Invalid email or password") is never shown — the
     * page just silently reloads. Returning 400 lets the catch block in LoginPage
     * display the error normally without triggering the redirect interceptor.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle missing required @RequestParam → 400 Bad Request.
     *
     * EXAMPLE: POST /api/classrooms without ?teacherId= in the URL.
     * Spring throws MissingServletRequestParameterException.
     * We return a clear message identifying which parameter is missing.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Required parameter '" + ex.getParameterName() + "' is missing from the request.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle type conversion failures for @RequestParam → 400 Bad Request.
     *
     * EXAMPLE: ?teacherId=null  (string "null" cannot be parsed as Long).
     * This happens if localStorage returns the literal string "null"
     * instead of a real numeric ID.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid value '" + ex.getValue()
                + "' for parameter '" + ex.getName()
                + "'. Expected type: " + ex.getRequiredType().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle @Valid validation failures on @RequestBody DTOs → 400.
     *
     * EXAMPLE: ClassroomRequest with a blank name field.
     * Returns a field-level map so the frontend knows exactly which field failed.
     *
     * Response format:
     * { "name": "Classroom name is required" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName    = ((FieldError) err).getField();
            String errorMessage = err.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handle all other unexpected runtime errors → 400 Bad Request.
     *
     * This is the LAST resort handler. Specific exceptions above take priority.
     * Returns the exception message so the client can see what went wrong.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
