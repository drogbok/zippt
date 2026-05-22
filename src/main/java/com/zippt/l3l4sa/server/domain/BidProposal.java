package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.validation.DataDictionaryValidator;
import com.zippt.l3l4sa.common.validation.DomainValidationException;
import com.zippt.l3l4sa.common.validation.ValidationErrorCode;
import java.math.BigDecimal;

public class BidProposal {
    private final String bidProposalId;
    private final String bidId;
    private final BigDecimal commissionRate;
    private final String marketingStrategy;
    private final int expectedSalePeriodDays;
    private final String serviceTerms;

    public BidProposal(String bidProposalId, String bidId, BigDecimal commissionRate,
                       String marketingStrategy, int expectedSalePeriodDays, String serviceTerms) {
        this.bidProposalId = bidProposalId;
        this.bidId = bidId;
        this.commissionRate = commissionRate;
        this.marketingStrategy = marketingStrategy;
        this.expectedSalePeriodDays = expectedSalePeriodDays;
        this.serviceTerms = serviceTerms;
    }

    public boolean validateRequiredFields() {
        DataDictionaryValidator.requireTextLength(
                marketingStrategy, 20, 1000, ValidationErrorCode.TEXT_LENGTH_INVALID);
        DataDictionaryValidator.requireTextLength(
                serviceTerms, 10, 500, ValidationErrorCode.TEXT_LENGTH_INVALID);
        if (expectedSalePeriodDays < 1 || expectedSalePeriodDays > 365) {
            throw new DomainValidationException(
                    ValidationErrorCode.TEXT_LENGTH_INVALID,
                    "Expected sale period must be between 1 and 365 days."
            );
        }
        return true;
    }

    public boolean validateCommissionRate() {
        DataDictionaryValidator.requireDecimalRange(
                commissionRate, BigDecimal.ZERO, BigDecimal.TEN, ValidationErrorCode.BID_COMMISSION_RATE_INVALID);
        DataDictionaryValidator.requireScale(commissionRate, 2, ValidationErrorCode.DECIMAL_SCALE_INVALID);
        return true;
    }

    public String getBidProposalId() { return bidProposalId; }
    public String getBidId() { return bidId; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public String getMarketingStrategy() { return marketingStrategy; }
    public int getExpectedSalePeriodDays() { return expectedSalePeriodDays; }
    public String getServiceTerms() { return serviceTerms; }
}


