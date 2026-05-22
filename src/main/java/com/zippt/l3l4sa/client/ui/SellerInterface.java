package com.zippt.l3l4sa.client.ui;

import com.zippt.l3l4sa.common.command.Commands.AuctionConditionInput;
import com.zippt.l3l4sa.common.command.Commands.CriteriaInput;

public class SellerInterface {
    public void requestAuctionRegistration(String sellerId) {
        showMessage("Seller requests auction registration: " + sellerId);
    }

    public void selectProperty(String propertyId) {
        showMessage("Seller selected property: " + propertyId);
    }

    public void enterAuctionCondition(AuctionConditionInput conditionData) {
        showMessage("Seller entered auction condition: " + conditionData);
    }

    public void enterWinnerSelectionCriteria(CriteriaInput criteriaInput) {
        showMessage("Seller entered winner criteria: " + criteriaInput);
    }

    public void confirmAuctionRegistration() {
        showMessage("Seller confirmed auction registration.");
    }

    public void requestClosedAuctionList(String sellerId) {
        showMessage("Seller requests closed auctions: " + sellerId);
    }

    public void selectWinner(String auctionId, String bidId) {
        showMessage("Seller selected winner bid " + bidId + " for auction " + auctionId);
    }

    public void postponeWinnerSelection(String auctionId) {
        showMessage("Seller postponed winner selection: " + auctionId);
    }

    public void showMessage(String message) {
        System.out.println("[SellerInterface] " + message);
    }
}


