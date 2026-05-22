package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.BidStatus;
import java.time.LocalDateTime;

public class Bid {
    private final String bidId;
    private final String auctionId;
    private final String agentId;
    private LocalDateTime submittedAt;
    private BidStatus status;
    private final String receiptNumber;
    private boolean resubmitted;
    private BidProposal proposal;

    public Bid(String bidId, String auctionId, String agentId, BidProposal proposal) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.agentId = agentId;
        this.proposal = proposal;
        this.status = BidStatus.DRAFT;
        this.receiptNumber = "RCPT-" + bidId;
    }

    public void submit(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
        this.status = BidStatus.SUBMITTED;
    }

    public void resubmit(BidProposal proposal, LocalDateTime submittedAt) {
        this.proposal = proposal;
        this.submittedAt = submittedAt;
        this.resubmitted = true;
        this.status = BidStatus.SUBMITTED;
    }

    public void markWon() {
        status = BidStatus.WON;
    }

    public void markLost() {
        status = BidStatus.LOST;
    }

    public void cancel() {
        status = BidStatus.CANCELLED;
    }

    public boolean isResubmitAllowed() {
        return status == BidStatus.RESUBMIT_ALLOWED;
    }

    public boolean belongsToAuction(String auctionId) {
        return this.auctionId.equals(auctionId);
    }

    public boolean submittedBy(String agentId) {
        return this.agentId.equals(agentId);
    }

    public String getBidId() { return bidId; }
    public String getAuctionId() { return auctionId; }
    public String getAgentId() { return agentId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public BidStatus getStatus() { return status; }
    public String getReceiptNumber() { return receiptNumber; }
    public boolean isResubmitted() { return resubmitted; }
    public BidProposal getProposal() { return proposal; }
}


