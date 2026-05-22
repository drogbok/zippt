package com.zippt.l3l4.server.domain;

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
        return commissionRate != null
                && marketingStrategy != null && !marketingStrategy.isBlank()
                && serviceTerms != null && !serviceTerms.isBlank()
                && expectedSalePeriodDays > 0;
    }

    public boolean validateCommissionRate() {
        return commissionRate != null
                && commissionRate.compareTo(BigDecimal.ZERO) >= 0
                && commissionRate.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    public String getBidProposalId() { return bidProposalId; }
    public String getBidId() { return bidId; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public String getMarketingStrategy() { return marketingStrategy; }
    public int getExpectedSalePeriodDays() { return expectedSalePeriodDays; }
    public String getServiceTerms() { return serviceTerms; }
}

