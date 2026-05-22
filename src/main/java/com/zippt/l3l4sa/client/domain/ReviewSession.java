package com.zippt.l3l4sa.client.domain;

import com.zippt.l3l4sa.common.command.Commands.RegisterReviewCommand;

public class ReviewSession {
    private String buyerId;
    private String reservationId;
    private int rating;
    private String text;

    public void storeReviewInput(String buyerId, String reservationId, int rating, String text) {
        this.buyerId = buyerId;
        this.reservationId = reservationId;
        this.rating = rating;
        this.text = text;
    }

    public RegisterReviewCommand toRegisterReviewCommand() {
        return new RegisterReviewCommand(buyerId, reservationId, rating, text);
    }

    public void discard() {
        buyerId = null;
        reservationId = null;
        rating = 0;
        text = null;
    }
}


