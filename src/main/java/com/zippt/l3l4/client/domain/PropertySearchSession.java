package com.zippt.l3l4.client.domain;

import com.zippt.l3l4.common.command.Commands.PropertyConditionInput;
import com.zippt.l3l4.common.command.Commands.SearchPropertyCommand;

public class PropertySearchSession {
    private String buyerId;
    private PropertyConditionInput conditionInput;

    public void storeBuyer(String buyerId) {
        this.buyerId = buyerId;
    }

    public void storeSearchCondition(PropertyConditionInput input) {
        this.conditionInput = input;
    }

    public SearchPropertyCommand toSearchPropertyCommand() {
        return new SearchPropertyCommand(buyerId, conditionInput);
    }

    public void discard() {
        buyerId = null;
        conditionInput = null;
    }
}

