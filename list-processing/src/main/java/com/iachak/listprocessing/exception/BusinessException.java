package com.iachak.listprocessing.exception;

// ─── Exception de base métier ─────────────────────────────────────────────────
// Toutes les exceptions métier héritent de cette classe.
// Elle permet au GlobalExceptionHandler de les distinguer des exceptions système.

public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
