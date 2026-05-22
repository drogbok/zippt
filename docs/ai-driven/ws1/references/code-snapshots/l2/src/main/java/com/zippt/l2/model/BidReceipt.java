package com.zippt.l2.model;

import java.time.LocalDateTime;

/**
 * [L2] Value Object : 입찰 접수증 (Description 11단계).
 */
public final class BidReceipt {

    private final String bidId;
    private final LocalDateTime timestamp;

    public BidReceipt(String bidId, LocalDateTime timestamp) {
        this.bidId = bidId;
        this.timestamp = timestamp;
    }

    public String getBidId()            { return bidId; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "BidReceipt{bidId=" + bidId + ", timestamp=" + timestamp + "}";
    }
}
