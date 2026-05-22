package com.zippt.l3l4sa.client.domain;

import com.zippt.l3l4sa.common.command.Commands.SelectWinnerCommand;

public class WinnerSelectionSession {
    private String sellerId;
    private String selectedAuctionId;
    private String selectedBidId;

    public void storeSeller(String sellerId) {
        this.sellerId = sellerId;
    }

    public void storeSelectedAuction(String auctionId) {
        this.selectedAuctionId = auctionId;
    }

    public void storeSelectedBid(String bidId) {
        this.selectedBidId = bidId;
    }

    public SelectWinnerCommand toSelectWinnerCommand() {
        return new SelectWinnerCommand(sellerId, selectedAuctionId, selectedBidId);
    }

    public void discard() {
        sellerId = null;
        selectedAuctionId = null;
        selectedBidId = null;
    }
}


