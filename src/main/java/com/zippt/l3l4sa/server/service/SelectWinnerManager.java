package com.zippt.l3l4sa.server.service;

import com.zippt.l3l4sa.common.command.Commands.SelectWinnerCommand;
import com.zippt.l3l4sa.common.enums.NotificationType;
import com.zippt.l3l4sa.common.result.Results.WinnerSelectionResult;
import com.zippt.l3l4sa.server.control.AuctionLifecycleControl;
import com.zippt.l3l4sa.server.domain.Auction;
import com.zippt.l3l4sa.server.domain.Bid;
import com.zippt.l3l4sa.server.domain.Notification;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SelectWinnerManager {
    private final DataStore store;
    private final AuthenticationManager authenticationManager;
    private final AuctionLifecycleControl lifecycleControl;

    public SelectWinnerManager(DataStore store, AuthenticationManager authenticationManager,
                               AuctionLifecycleControl lifecycleControl) {
        this.store = store;
        this.authenticationManager = authenticationManager;
        this.lifecycleControl = lifecycleControl;
    }

    public WinnerSelectionResult selectWinner(SelectWinnerCommand command) {
        authenticationManager.validateSeller(command.sellerId());
        synchronized (store.lockFor("auction:" + command.auctionId())) {
            Auction auction = validateAuctionClosed(command.auctionId());
            validateNoWinnerSelected(auction);
            List<Bid> bids = sortBidsByCriteria(auction);
            if (bids.isEmpty()) {
                throw new IllegalStateException("No bids for auction.");
            }
            markWinner(auction, command.selectedBidId());
            List<String> losingBidIds = store.bids().stream()
                    .filter(bid -> bid.belongsToAuction(command.auctionId()))
                    .filter(bid -> !bid.getBidId().equals(command.selectedBidId()))
                    .map(Bid::getBidId)
                    .toList();
            notifyBidResults(auction);
            return new WinnerSelectionResult(auction.getAuctionId(), command.selectedBidId(), losingBidIds);
        }
    }

    public Auction validateAuctionClosed(String auctionId) {
        Auction auction = store.findAuction(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Auction not found: " + auctionId);
        }
        if (!auction.isClosedForBidding(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Auction is not closed.");
        }
        if (auction.hasActiveStatus()) {
            auction.close();
        }
        return auction;
    }

    public boolean validateNoWinnerSelected(Auction auction) {
        if (auction.hasWinnerSelected()) {
            throw new IllegalStateException("Winner already selected.");
        }
        return true;
    }

    public List<Bid> sortBidsByCriteria(Auction auction) {
        return store.bids().stream()
                .filter(bid -> bid.belongsToAuction(auction.getAuctionId()))
                .sorted(Comparator.comparing(
                        bid -> auction.getSelectionCriteria().calculateBidScore(bid.getProposal()),
                        Comparator.reverseOrder()
                ))
                .toList();
    }

    public void markWinner(Auction auction, String selectedBidId) {
        List<Bid> bids = new ArrayList<>(store.bids().stream()
                .filter(bid -> bid.belongsToAuction(auction.getAuctionId()))
                .toList());
        boolean found = false;
        for (Bid bid : bids) {
            if (bid.getBidId().equals(selectedBidId)) {
                bid.markWon();
                found = true;
            } else {
                bid.markLost();
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Selected bid does not belong to auction.");
        }
        lifecycleControl.markWinnerSelected(auction, selectedBidId);
    }

    public void notifyBidResults(Auction auction) {
        for (Bid bid : store.bids()) {
            if (bid.belongsToAuction(auction.getAuctionId())) {
                store.addNotification(new Notification(
                        "noti-" + UUID.randomUUID(),
                        bid.getAgentId(),
                        NotificationType.BID_RESULT,
                        "Bid result: " + bid.getStatus()
                ));
            }
        }
    }

    public void postponeWinnerSelection(String auctionId) {
        synchronized (store.lockFor("auction:" + auctionId)) {
            lifecycleControl.postponeSelection(validateAuctionClosed(auctionId));
        }
    }
}


