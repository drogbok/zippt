package com.zippt.l3l4sa.common.validation;

public class DomainValidationException extends RuntimeException {
    private final ValidationErrorCode errorCode;

    public DomainValidationException(ValidationErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ValidationErrorCode getErrorCode() {
        return errorCode;
    }
}

