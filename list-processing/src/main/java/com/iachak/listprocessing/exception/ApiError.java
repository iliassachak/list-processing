package com.iachak.listprocessing.exception;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Corps JSON uniforme renvoyé pour toute erreur HTTP.
 * Exemple :
 * {
 *   "timestamp": "2025-04-15T10:30:00",
 *   "status": 400,
 *   "error": "BAD_REQUEST",
 *   "message": "Username already taken",
 *   "path": "/api/auth/register"
 * }
 * */
public record ApiError(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path);
    }
}
