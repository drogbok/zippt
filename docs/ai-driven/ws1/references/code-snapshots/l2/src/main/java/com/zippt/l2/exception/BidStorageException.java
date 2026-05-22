package com.zippt.l2.exception;

/**
 * [L2] Alternative A4 : 저장소 쓰기 실패.
 */
public class BidStorageException extends Exception {
    public BidStorageException() {
        super("Bid storage failed (transient error)");
    }
    public BidStorageException(Throwable cause) {
        super("Bid storage failed (transient error)", cause);
    }
}
