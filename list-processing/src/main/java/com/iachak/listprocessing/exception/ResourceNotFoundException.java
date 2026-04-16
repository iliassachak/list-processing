package com.iachak.listprocessing.exception;

/**
 * Levée quand une entité demandée est introuvable en base.
 * → HTTP 404 Not Found
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " introuvable : " + id);
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
