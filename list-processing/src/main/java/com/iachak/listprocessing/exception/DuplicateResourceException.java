package com.iachak.listprocessing.exception;

/**
 * Levée quand une ressource existe déjà (unicité violée).
 * → HTTP 409 Conflict
 */
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) { super(message); }
}
