package com.zippt.l3l4.server.domain;

import java.time.LocalDateTime;

public class AuctionCondition {
    private final String auctionConditionId;
    private final String auctionId;
    private final String serviceCondition;
    private final String minQualification;
    private final LocalDateTime bidDeadline;

    public AuctionCondition(String auctionConditionId, String auctionId,
                            String serviceCondition, String minQualification,
                            LocalDateTime bidDeadline) {
        this.auctionConditionId = auctionConditionId;
        this.auctionId = auctionId;
        this.serviceCondition = serviceCondition;
        this.minQualification = minQualification;
        this.bidDeadline = bidDeadline;
    }

    public boolean validateRequiredFields() {
        return serviceCondition != null && !serviceCondition.isBlank()
                && minQualification != null && !minQualification.isBlank()
                && bidDeadline != null;
    }

    public boolean validateDeadlineRange(LocalDateTime now) {
        return bidDeadline != null && bidDeadline.isAfter(now);
    }

    public String getAuctionConditionId() { return auctionConditionId; }
    public String getAuctionId() { return auctionId; }
    public String getServiceCondition() { return serviceCondition; }
    public String getMinQualification() { return minQualification; }
    public LocalDateTime getBidDeadline() { return bidDeadline; }
}

