package com.zippt.l3l4sa.server.service;

import com.zippt.l3l4sa.common.command.Commands.SubmitBidCommand;
import com.zippt.l3l4sa.common.enums.NotificationType;
import com.zippt.l3l4sa.common.result.Results.BidSubmissionResult;
import com.zippt.l3l4sa.server.domain.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class SubmitBidManager {
    private final DataStore store;
    private final AuthenticationManager authenticationManager;

    public SubmitBidManager(DataStore store, AuthenticationManager authenticationManager) {
        this.store = store;
        this.authenticationManager = authenticationManager;
    }

    public BidSubmissionResult submitBid(SubmitBidCommand command) {
        Agent agent = authenticationManager.validateAgent(command.agentId());
        validateAgentCredential(agent.getUserId());

        synchronized (store.lockFor("auction:" + command.auctionId())) {
            Auction auction = validateAuctionOpen(command.auctionId());
            BidProposal proposal = new BidProposal(
                    "proposal-" + UUID.randomUUID(),
                    "pending",
                    command.proposalInput().commissionRate(),
                    command.proposalInput().marketingStrategy(),
                    command.proposalInput().expectedSalePeriodDays(),
                    command.proposalInput().serviceTerms()
            );
            if (!validateBidProposal(proposal)) {
                throw new IllegalArgumentException("Invalid bid proposal.");
            }

            Bid bid = createOrUpdateBid(command, proposal);
            if (!bid.isResubmitted()) {
                auction.incrementBidCount();
            }
            notifySellerOfNewBid(auction, bid);
            return new BidSubmissionResult(bid.getBidId(), bid.getReceiptNumber(), bid.getSubmittedAt());
        }
    }

    public Auction validateAuctionOpen(String auctionId) {
        Auction auction = store.findAuction(auctionId);
        if (auction == null || !auction.hasActiveStatus() || auction.isClosedForBidding(LocalDateTime.now())) {
            throw new IllegalStateException("Auction is not open for bidding.");
        }
        return auction;
    }

    public AgentCredential validateAgentCredential(String agentId) {
        AgentCredential credential = store.findCredentialByAgent(agentId);
        if (credential == null) {
            throw new IllegalStateException("Agent credential is not verified.");
        }
        credential.ensureBidEligible();
        return credential;
    }

    public boolean validateBidProposal(BidProposal proposal) {
        return proposal.validateRequiredFields() && proposal.validateCommissionRate();
    }

    public Bid createOrUpdateBid(SubmitBidCommand command, BidProposal proposal) {
        Bid existing = store.bids().stream()
                .filter(bid -> bid.belongsToAuction(command.auctionId()))
                .filter(bid -> bid.submittedBy(command.agentId()))
                .findFirst()
                .orElse(null);
        if (existing != null && existing.isResubmitAllowed()) {
            existing.resubmit(proposal, LocalDateTime.now());
            return existing;
        }
        if (existing != null) {
            throw new IllegalStateException("Agent already submitted bid for this auction.");
        }
        Bid bid = new Bid("bid-" + UUID.randomUUID(), command.auctionId(), command.agentId(), proposal);
        bid.submit(LocalDateTime.now());
        store.saveBid(bid);
        return bid;
    }

    public void notifySellerOfNewBid(Auction auction, Bid bid) {
        store.addNotification(new Notification(
                "noti-" + UUID.randomUUID(),
                auction.getSellerId(),
                NotificationType.NEW_BID,
                "New bid submitted: " + bid.getBidId()
        ));
    }
}


