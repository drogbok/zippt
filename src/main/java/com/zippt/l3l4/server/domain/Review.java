package com.zippt.l3l4.server.domain;

import java.time.LocalDateTime;

public class Review {
    private final String reviewId;
    private final String reservationId;
    private final String buyerId;
    private final String agentId;
    private final String propertyId;
    private final int rating;
    private final String text;
    private final LocalDateTime createdAt;

    public Review(String reviewId, String reservationId, String buyerId,
                  String agentId, String propertyId, int rating, String text) {
        this.reviewId = reviewId;
        this.reservationId = reservationId;
        this.buyerId = buyerId;
        this.agentId = agentId;
        this.propertyId = propertyId;
        this.rating = rating;
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    public boolean validateRating() {
        return rating >= 1 && rating <= 5;
    }

    public boolean attachToReservation(String reservationId) {
        return this.reservationId.equals(reservationId);
    }

    public String getReviewId() { return reviewId; }
    public String getReservationId() { return reservationId; }
    public String getBuyerId() { return buyerId; }
    public String getAgentId() { return agentId; }
    public String getPropertyId() { return propertyId; }
    public int getRating() { return rating; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

