package com.zippt.l3l4sa.client.domain;

import com.zippt.l3l4sa.common.command.Commands.AuctionConditionInput;
import com.zippt.l3l4sa.common.command.Commands.CriteriaInput;
import com.zippt.l3l4sa.common.command.Commands.RegisterAuctionCommand;

public class AuctionSession {
    private String sellerId;
    private String selectedPropertyId;
    private AuctionConditionInput conditionInput;
    private CriteriaInput criteriaInput;

    public void storeSeller(String sellerId) {
        this.sellerId = sellerId;
    }

    public void storeSelectedProperty(String propertyId) {
        this.selectedPropertyId = propertyId;
    }

    public void storeAuctionCondition(AuctionConditionInput input) {
        this.conditionInput = input;
    }

    public void storeWinnerSelectionCriteria(CriteriaInput input) {
        this.criteriaInput = input;
    }

    public RegisterAuctionCommand toRegisterAuctionCommand() {
        return new RegisterAuctionCommand(sellerId, selectedPropertyId, conditionInput, criteriaInput);
    }

    public void discard() {
        sellerId = null;
        selectedPropertyId = null;
        conditionInput = null;
        criteriaInput = null;
    }
}


