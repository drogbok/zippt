package com.zippt.l2.exception;

/**
 * [L2] Alternative A2 : 입력 유효성 검증 실패.
 */
public class BidValidationException extends Exception {
    public BidValidationException(String message) {
        super(message);
    }
}
