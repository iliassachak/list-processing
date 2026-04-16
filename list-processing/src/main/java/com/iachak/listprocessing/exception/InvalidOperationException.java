package com.iachak.listprocessing.exception;

/**
 * Levée quand une opération est sémantiquement invalide :
 * - désactivation du compte admin protégé
 * - fichier Excel vide
 * - colonnes incompatibles lors d'un append
 * - mot de passe trop court
 * → HTTP 400 Bad Request
 */
public class InvalidOperationException extends BusinessException {
    public InvalidOperationException(String message) { super(message); }
}
