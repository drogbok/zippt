package com.zippt.l3l4.server.service;

import com.zippt.l3l4.common.command.Commands.*;
import com.zippt.l3l4.common.result.Results.*;
import com.zippt.l3l4.server.control.AuctionLifecycleControl;

public class ZIPPTTransactionServer {
    private final RegisterAuctionManager registerAuctionManager;
    private final SubmitBidManager submitBidManager;
    private final SelectWinnerManager selectWinnerManager;
    private final SearchPropertyManager searchPropertyManager;
    private final ReservationManager reservationManager;
    private final ReviewManager reviewManager;

    public ZIPPTTransactionServer(DataStore store) {
        AuthenticationManager authenticationManager = new AuthenticationManager(store);
        AuctionLifecycleControl lifecycleControl = new AuctionLifecycleControl();
        this.registerAuctionManager = new RegisterAuctionManager(store, authenticationManager, lifecycleControl);
        this.submitBidManager = new SubmitBidManager(store, authenticationManager);
        this.selectWinnerManager = new SelectWinnerManager(store, authenticationManager, lifecycleControl);
        this.searchPropertyManager = new SearchPropertyManager(store, authenticationManager);
        this.reservationManager = new ReservationManager(authenticationManager);
        this.reviewManager = new ReviewManager(authenticationManager);
    }

    public AuctionRegistrationResult handleRegisterAuction(RegisterAuctionCommand command) {
        return registerAuctionManager.registerAuction(command);
    }

    public BidSubmissionResult handleSubmitBid(SubmitBidCommand command) {
        return submitBidManager.submitBid(command);
    }

    public WinnerSelectionResult handleSelectWinner(SelectWinnerCommand command) {
        return selectWinnerManager.selectWinner(command);
    }

    public PropertySearchResult handleSearchProperty(SearchPropertyCommand command) {
        return searchPropertyManager.search(command);
    }

    public ReservationResult handleRequestVisit(RequestVisitCommand command) {
        return reservationManager.requestVisit(command);
    }

    public ReviewResult handleRegisterReview(RegisterReviewCommand command) {
        return reviewManager.registerReview(command);
    }
}

