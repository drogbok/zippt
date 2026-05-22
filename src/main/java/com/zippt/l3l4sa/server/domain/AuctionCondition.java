package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.validation.DataDictionaryValidator;
import com.zippt.l3l4sa.common.validation.ValidationErrorCode;
import java.time.Duration;
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
        DataDictionaryValidator.requireTextLength(
                serviceCondition, 10, 500, ValidationErrorCode.TEXT_LENGTH_INVALID);
        DataDictionaryValidator.requireTextLength(
                minQualification, 5, 300, ValidationErrorCode.TEXT_LENGTH_INVALID);
        return bidDeadline != null;
    }

    public boolean validateDeadlineRange(LocalDateTime now) {
        DataDictionaryValidator.requireFutureWithin(
                bidDeadline, Duration.ofHours(1), Duration.ofDays(90), ValidationErrorCode.AUCTION_DEADLINE_INVALID);
        return true;
    }

    public String getAuctionConditionId() { return auctionConditionId; }
    public String getAuctionId() { return auctionId; }
    public String getServiceCondition() { return serviceCondition; }
    public String getMinQualification() { return minQualification; }
    public LocalDateTime getBidDeadline() { return bidDeadline; }
}


