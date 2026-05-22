package com.zippt.l3l4.common.result;

import java.time.LocalDateTime;
import java.util.List;

public final class Results {
    private Results() {
    }

    public record AuctionRegistrationResult(String auctionId, String message) {
    }

    public record BidSubmissionResult(String bidId, String receiptNumber, LocalDateTime submittedAt) {
    }

    public record WinnerSelectionResult(String auctionId, String winningBidId, List<String> losingBidIds) {
    }

    public record PropertySearchResult(List<String> propertyIds) {
    }

    public record ReservationResult(String reservationId, String status) {
    }

    public record ReviewResult(String reviewId, String status) {
    }
}

