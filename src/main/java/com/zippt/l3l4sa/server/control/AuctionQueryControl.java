package com.zippt.l3l4sa.server.control;

import com.zippt.l3l4sa.common.enums.AuctionStatus;
import com.zippt.l3l4sa.server.domain.Auction;
import com.zippt.l3l4sa.server.domain.Bid;
import com.zippt.l3l4sa.server.domain.Property;
import com.zippt.l3l4sa.server.service.DataStore;
import java.util.List;

public class AuctionQueryControl {
    private final DataStore store;

    public AuctionQueryControl(DataStore store) {
        this.store = store;
    }

    public List<Property> getSellerProperties(String sellerId) {
        return store.properties().stream()
                .filter(property -> property.isOwnedBy(sellerId))
                .toList();
    }

    public List<Auction> getOpenAuctionsForAgent(String agentId) {
        return store.auctions().stream()
                .filter(auction -> auction.getStatus() == AuctionStatus.OPEN)
                .toList();
    }

    public List<Auction> getClosedAuctionsForSeller(String sellerId) {
        return store.auctions().stream()
                .filter(auction -> auction.getSellerId().equals(sellerId))
                .filter(auction -> auction.getStatus() == AuctionStatus.CLOSED
                        || auction.getStatus() == AuctionStatus.PENDING_WINNER)
                .toList();
    }

    public Auction getAuctionDetail(String auctionId) {
        return store.findAuction(auctionId);
    }

    public List<Bid> getBidsForAuction(String auctionId) {
        return store.bids().stream()
                .filter(bid -> bid.belongsToAuction(auctionId))
                .toList();
    }
}


