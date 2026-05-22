package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.WinnerPriorityType;
import com.zippt.l3l4sa.common.validation.DataDictionaryValidator;
import com.zippt.l3l4sa.common.validation.DomainValidationException;
import com.zippt.l3l4sa.common.validation.ValidationErrorCode;
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
        DataDictionaryValidator.requireDecimalRange(
                commissionRateWeight, BigDecimal.ZERO, BigDecimal.ONE, ValidationErrorCode.CRITERIA_WEIGHT_INVALID);
        DataDictionaryValidator.requireDecimalRange(
                marketingStrategyWeight, BigDecimal.ZERO, BigDecimal.ONE, ValidationErrorCode.CRITERIA_WEIGHT_INVALID);
        DataDictionaryValidator.requireEqual(
                commissionRateWeight.add(marketingStrategyWeight),
                BigDecimal.ONE,
                ValidationErrorCode.CRITERIA_WEIGHT_INVALID);
        validatePriorityWeightRule();
        return true;
    }

    private void validatePriorityWeightRule() {
        if (priorityType == WinnerPriorityType.PRICE_FIRST
                && commissionRateWeight.compareTo(BigDecimal.valueOf(0.6)) < 0) {
            throw new DomainValidationException(
                    ValidationErrorCode.CRITERIA_PRIORITY_WEIGHT_INVALID,
                    "PRICE_FIRST requires commissionRateWeight >= 0.6."
            );
        }
        if (priorityType == WinnerPriorityType.SERVICE_FIRST
                && marketingStrategyWeight.compareTo(BigDecimal.valueOf(0.6)) < 0) {
            throw new DomainValidationException(
                    ValidationErrorCode.CRITERIA_PRIORITY_WEIGHT_INVALID,
                    "SERVICE_FIRST requires marketingStrategyWeight >= 0.6."
            );
        }
        if (priorityType == WinnerPriorityType.BALANCED
                && (commissionRateWeight.compareTo(BigDecimal.valueOf(0.4)) < 0
                || commissionRateWeight.compareTo(BigDecimal.valueOf(0.6)) > 0)) {
            throw new DomainValidationException(
                    ValidationErrorCode.CRITERIA_PRIORITY_WEIGHT_INVALID,
                    "BALANCED requires each weight to be between 0.4 and 0.6."
            );
        }
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


