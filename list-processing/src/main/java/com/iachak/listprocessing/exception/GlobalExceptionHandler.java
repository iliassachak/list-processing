package com.iachak.listprocessing.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.NoSuchElementException;

/**
 * Gestionnaire global des exceptions. Intercèpte toutes les exceptions
 * non gérées dans les contrôleurs et retourne un ApiError structuré.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 400 Bad Request ─────────────────────────────────────────────

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiError> handleInvalidOperation(
            InvalidOperationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "Fichier trop volumineux. Taille maximale autorisée : 50 MB.", req);
    }

    // ─── 401 Unauthorized ────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Identifiants incorrects.", req);
    }

    // ─── 403 Forbidden ───────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        String msg = ex.getMessage();
        // Personnaliser les messages Spring Security génériques
        if (msg == null || msg.equalsIgnoreCase("Access Denied")) {
            msg = "Vous n'avez pas les droits nécessaires pour effectuer cette action.";
        }
        return build(HttpStatus.FORBIDDEN, msg, req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(
            DisabledException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN,
                "Ce compte a été désactivé. Veuillez contacter un administrateur.", req);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLocked(
            LockedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Ce compte est verrouillé.", req);
    }

    // ─── 404 Not Found ───────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElement(
            NoSuchElementException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND,
                ex.getMessage() != null ? ex.getMessage() : "Ressource introuvable.", req);
    }

    // ─── 409 Conflict ────────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // ─── 500 Internal Server Error ───────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex, HttpServletRequest req) {
        // Log complet en serveur, message générique au client
        System.err.println("[ERROR] Unhandled exception on " + req.getRequestURI());
        ex.printStackTrace();
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue. Veuillez réessayer.", req);
    }

    // ─── Helper ──────────────────────────────────────────────────────

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest req) {
        ApiError body = ApiError.of(status.value(), status.name(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
