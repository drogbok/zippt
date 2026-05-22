package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.AuctionStatus;
import java.time.LocalDateTime;

public class Auction {
    private final String auctionId;
    private final String propertyId;
    private final String sellerId;
    private final LocalDateTime createdAt;
    private AuctionStatus status;
    private LocalDateTime bidDeadline;
    private int bidCount;
    private String selectedBidId;
    private AuctionCondition condition;
    private WinnerSelectionCriteria selectionCriteria;

    public Auction(String auctionId, String propertyId, String sellerId,
                   AuctionCondition condition, WinnerSelectionCriteria selectionCriteria) {
        this.auctionId = auctionId;
        this.propertyId = propertyId;
        this.sellerId = sellerId;
        this.condition = condition;
        this.selectionCriteria = selectionCriteria;
        this.bidDeadline = condition.getBidDeadline();
        this.createdAt = LocalDateTime.now();
        this.status = AuctionStatus.DRAFT;
    }

    public void open() {
        status = AuctionStatus.OPEN;
    }

    public void close() {
        status = AuctionStatus.CLOSED;
    }

    public void postponeWinnerSelection() {
        status = AuctionStatus.PENDING_WINNER;
    }

    public void markWinnerSelected(String selectedBidId) {
        this.selectedBidId = selectedBidId;
        status = AuctionStatus.WINNER_SELECTED;
    }

    public void cancel() {
        status = AuctionStatus.CANCELLED;
    }

    public void incrementBidCount() {
        bidCount++;
    }

    public boolean hasActiveStatus() {
        return status == AuctionStatus.OPEN;
    }

    public boolean isClosedForBidding(LocalDateTime now) {
        return status == AuctionStatus.CLOSED || !now.isBefore(bidDeadline);
    }

    public boolean hasWinnerSelected() {
        return status == AuctionStatus.WINNER_SELECTED || selectedBidId != null;
    }

    public String getAuctionId() { return auctionId; }
    public String getPropertyId() { return propertyId; }
    public String getSellerId() { return sellerId; }
    public AuctionStatus getStatus() { return status; }
    public LocalDateTime getBidDeadline() { return bidDeadline; }
    public int getBidCount() { return bidCount; }
    public String getSelectedBidId() { return selectedBidId; }
    public AuctionCondition getCondition() { return condition; }
    public WinnerSelectionCriteria getSelectionCriteria() { return selectionCriteria; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


