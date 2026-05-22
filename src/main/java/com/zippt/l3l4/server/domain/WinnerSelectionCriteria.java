package com.zippt.l3l4.server.domain;

import com.zippt.l3l4.common.enums.WinnerPriorityType;
import java.math.BigDecimal;

public class WinnerSelectionCriteria {
    private final String criteriaId;
    private final String auctionId;
    private final WinnerPriorityType priorityType;
    private final BigDecimal commissionRateWeight;
    private final BigDecimal marketingStrategyWeight;

    public WinnerSelectionCriteria(String criteriaId, String auctionId, WinnerPriorityType priorityType,
                                   BigDecimal commissionRateWeight, BigDecimal marketingStrategyWeight) {
        this.criteriaId = criteriaId;
        this.auctionId = auctionId;
        this.priorityType = priorityType;
        this.commissionRateWeight = commissionRateWeight;
        this.marketingStrategyWeight = marketingStrategyWeight;
    }

    public boolean validateWeights() {
        BigDecimal total = commissionRateWeight.add(marketingStrategyWeight);
        return commissionRateWeight.signum() >= 0
                && marketingStrategyWeight.signum() >= 0
                && total.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal calculateBidScore(BidProposal proposal) {
        BigDecimal priceScore = BigDecimal.valueOf(100).subtract(proposal.getCommissionRate());
        BigDecimal serviceScore = BigDecimal.valueOf(Math.min(100, proposal.getMarketingStrategy().length()));
        return priceScore.multiply(commissionRateWeight).add(serviceScore.multiply(marketingStrategyWeight));
    }

    public boolean isPriceFirst() {
        return priorityType == WinnerPriorityType.PRICE_FIRST;
    }

    public boolean isServiceFirst() {
        return priorityType == WinnerPriorityType.SERVICE_FIRST;
    }

    public String getCriteriaId() { return criteriaId; }
    public String getAuctionId() { return auctionId; }
    public WinnerPriorityType getPriorityType() { return priorityType; }
}

