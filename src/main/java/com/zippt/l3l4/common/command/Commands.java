package com.zippt.l3l4.common.command;

import com.zippt.l3l4.common.enums.PropertyType;
import com.zippt.l3l4.common.enums.WinnerPriorityType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Commands {
    private Commands() {
    }

    public record AuctionConditionInput(
            LocalDateTime bidDeadline,
            String serviceCondition,
            String minQualification
    ) {
    }

    public record CriteriaInput(
            WinnerPriorityType priorityType,
            BigDecimal commissionRateWeight,
            BigDecimal marketingStrategyWeight
    ) {
    }

    public record BidProposalInput(
            BigDecimal commissionRate,
            String marketingStrategy,
            int expectedSalePeriodDays,
            String serviceTerms
    ) {
    }

    public record PropertyConditionInput(
            String region,
            BigDecimal priceMin,
            BigDecimal priceMax,
            BigDecimal areaMin,
            BigDecimal areaMax,
            String naturalLanguageQuery,
            PropertyType propertyType
    ) {
    }

    public record RegisterAuctionCommand(
            String sellerId,
            String propertyId,
            AuctionConditionInput conditionInput,
            CriteriaInput criteriaInput
    ) {
    }

    public record SubmitBidCommand(
            String agentId,
            String auctionId,
            BidProposalInput proposalInput
    ) {
    }

    public record SelectWinnerCommand(
            String sellerId,
            String auctionId,
            String selectedBidId
    ) {
    }

    public record SearchPropertyCommand(String buyerId, PropertyConditionInput conditionInput) {
    }

    public record RequestVisitCommand(String buyerId, String propertyId, String agentId, LocalDateTime visitAt) {
    }

    public record RegisterReviewCommand(String buyerId, String reservationId, int rating, String text) {
    }
}

