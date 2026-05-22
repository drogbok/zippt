package com.zippt.l3l4.client.ui;

import com.zippt.l3l4.common.command.Commands.PropertyConditionInput;
import java.time.LocalDateTime;

public class BuyerInterface {
    public void requestPropertySearch(PropertyConditionInput conditionInput) {
        showMessage("Buyer requests property search: " + conditionInput);
    }

    public void requestVisit(String propertyId, String agentId, LocalDateTime visitAt) {
        showMessage("Buyer requests visit: " + propertyId + ", " + agentId + ", " + visitAt);
    }

    public void enterReview(String reservationId, int rating, String text) {
        showMessage("Buyer entered review: " + reservationId + ", rating=" + rating);
    }

    public void showMessage(String message) {
        System.out.println("[BuyerInterface] " + message);
    }
}

