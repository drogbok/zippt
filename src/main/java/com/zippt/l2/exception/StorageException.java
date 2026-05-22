package com.zippt.l2.exception;

/**
 * [L2] 저장소 계층 일반 예외.
 * <p>
 * BidRepository.save 에서 발생하며, SubmitBidUseCase 에서 포착되어
 * BidStorageException 으로 변환됨 (Alternative A4).
 */
public class StorageException extends Exception {
    public StorageException(String message) {
        super(message);
    }
    public StorageException(Throwable cause) {
        super(cause);
    }
}
