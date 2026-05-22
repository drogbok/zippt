package com.zippt.l3l4sa.client.control;

import com.zippt.l3l4sa.client.domain.AuctionSession;
import com.zippt.l3l4sa.client.domain.BidSession;
import com.zippt.l3l4sa.client.domain.WinnerSelectionSession;
import com.zippt.l3l4sa.common.command.Commands.AuctionConditionInput;
import com.zippt.l3l4sa.common.command.Commands.BidProposalInput;
import com.zippt.l3l4sa.common.command.Commands.CriteriaInput;
import com.zippt.l3l4sa.common.enums.AuctionClientState;
import com.zippt.l3l4sa.common.result.Results.AuctionRegistrationResult;
import com.zippt.l3l4sa.common.result.Results.BidSubmissionResult;
import com.zippt.l3l4sa.common.result.Results.WinnerSelectionResult;
import com.zippt.l3l4sa.server.service.ZIPPTTransactionServer;

public class AuctionClientControl {
    private final ZIPPTTransactionServer server;
    private AuctionClientState state = AuctionClientState.IDLE;
    private AuctionSession auctionSession;
    private BidSession bidSession;
    private WinnerSelectionSession winnerSelectionSession;

    public AuctionClientControl(ZIPPTTransactionServer server) {
        this.server = server;
    }

    public void startRegisterAuction(String sellerId) {
        requireIdle();
        auctionSession = new AuctionSession();
        auctionSession.storeSeller(sellerId);
        state = AuctionClientState.REGISTERING_AUCTION;
    }

    public void propertySelected(String propertyId) {
        ensureState(AuctionClientState.REGISTERING_AUCTION);
        auctionSession.storeSelectedProperty(propertyId);
    }

    public void auctionConditionEntered(AuctionConditionInput input) {
        ensureState(AuctionClientState.REGISTERING_AUCTION);
        auctionSession.storeAuctionCondition(input);
    }

    public void criteriaEntered(CriteriaInput input) {
        ensureState(AuctionClientState.REGISTERING_AUCTION);
        auctionSession.storeWinnerSelectionCriteria(input);
    }

    public AuctionRegistrationResult confirmAuctionRegistration() {
        ensureState(AuctionClientState.REGISTERING_AUCTION);
        state = AuctionClientState.WAITING_SERVER_RESPONSE;
        AuctionRegistrationResult result = server.handleRegisterAuction(auctionSession.toRegisterAuctionCommand());
        state = AuctionClientState.COMPLETED;
        return result;
    }

    public void startSubmitBid(String agentId) {
        requireIdleOrCompleted();
        bidSession = new BidSession();
        bidSession.storeAgent(agentId);
        state = AuctionClientState.WRITING_BID;
    }

    public void auctionSelected(String auctionId) {
        ensureState(AuctionClientState.WRITING_BID);
        bidSession.storeSelectedAuction(auctionId);
    }

    public void bidProposalEntered(BidProposalInput input) {
        ensureState(AuctionClientState.WRITING_BID);
        bidSession.storeBidProposal(input);
    }

    public BidSubmissionResult confirmBidSubmission() {
        ensureState(AuctionClientState.WRITING_BID);
        state = AuctionClientState.WAITING_SERVER_RESPONSE;
        BidSubmissionResult result = server.handleSubmitBid(bidSession.toSubmitBidCommand());
        state = AuctionClientState.COMPLETED;
        return result;
    }

    public void startSelectWinner(String sellerId) {
        requireIdleOrCompleted();
        winnerSelectionSession = new WinnerSelectionSession();
        winnerSelectionSession.storeSeller(sellerId);
        state = AuctionClientState.SELECTING_WINNER;
    }

    public void winnerSelected(String auctionId, String bidId) {
        ensureState(AuctionClientState.SELECTING_WINNER);
        winnerSelectionSession.storeSelectedAuction(auctionId);
        winnerSelectionSession.storeSelectedBid(bidId);
    }

    public WinnerSelectionResult confirmWinnerSelection() {
        ensureState(AuctionClientState.SELECTING_WINNER);
        state = AuctionClientState.WAITING_SERVER_RESPONSE;
        WinnerSelectionResult result = server.handleSelectWinner(winnerSelectionSession.toSelectWinnerCommand());
        state = AuctionClientState.COMPLETED;
        return result;
    }

    public void cancelCurrentFlow() {
        if (auctionSession != null) auctionSession.discard();
        if (bidSession != null) bidSession.discard();
        if (winnerSelectionSession != null) winnerSelectionSession.discard();
        state = AuctionClientState.CANCELLED;
    }

    private void requireIdle() {
        ensureState(AuctionClientState.IDLE);
    }

    private void requireIdleOrCompleted() {
        if (state != AuctionClientState.IDLE && state != AuctionClientState.COMPLETED) {
            throw new IllegalStateException("Client control is busy: " + state);
        }
    }

    private void ensureState(AuctionClientState expected) {
        if (state != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + state);
        }
    }

    public AuctionClientState getState() {
        return state;
    }
}


