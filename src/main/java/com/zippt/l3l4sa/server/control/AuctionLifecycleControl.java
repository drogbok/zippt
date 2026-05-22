package com.zippt.l3l4sa.server.control;

import com.zippt.l3l4sa.common.enums.AuctionStatus;
import com.zippt.l3l4sa.server.domain.Auction;

public class AuctionLifecycleControl {
    public void openAuction(Auction auction) {
        requireStatus(auction, AuctionStatus.DRAFT);
        auction.open();
    }

    public void closeAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new IllegalStateException("Only OPEN auction can be closed.");
        }
        auction.close();
    }

    public void postponeSelection(Auction auction) {
        if (auction.getStatus() != AuctionStatus.CLOSED) {
            throw new IllegalStateException("Only CLOSED auction can be postponed.");
        }
        auction.postponeWinnerSelection();
    }

    public void markWinnerSelected(Auction auction, String selectedBidId) {
        if (auction.getStatus() != AuctionStatus.CLOSED
                && auction.getStatus() != AuctionStatus.PENDING_WINNER) {
            throw new IllegalStateException("Auction is not ready for winner selection.");
        }
        auction.markWinnerSelected(selectedBidId);
    }

    public void cancelAuction(Auction auction) {
        if (auction.getStatus() == AuctionStatus.WINNER_SELECTED) {
            throw new IllegalStateException("Winner selected auction cannot be cancelled.");
        }
        auction.cancel();
    }

    private void requireStatus(Auction auction, AuctionStatus expected) {
        if (auction.getStatus() != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + auction.getStatus());
        }
    }
}


