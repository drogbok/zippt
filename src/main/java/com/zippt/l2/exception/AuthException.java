package com.zippt.l2.exception;

/**
 * [L2] Precondition P1 / P3 위반 : 인증 / 자격 검증 실패.
 */
public class AuthException extends Exception {
    public AuthException() {
        super("Authentication or certification check failed");
    }
}
