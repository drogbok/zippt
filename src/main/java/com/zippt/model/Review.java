package com.zippt.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Review {
    private long id;
    private long reservationId;
    private long buyerId;
    private long agentId;
    private long propertyId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }

    public long getBuyerId() { return buyerId; }
    public void setBuyerId(long buyerId) { this.buyerId = buyerId; }

    public long getAgentId() { return agentId; }
    public void setAgentId(long agentId) { this.agentId = agentId; }

    public long getPropertyId() { return propertyId; }
    public void setPropertyId(long propertyId) { this.propertyId = propertyId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    private String stars() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    @Override
    public String toString() {
        return String.format("[후기#%d] 예약#%d | %s (%d/5) | %s | %s",
                id, reservationId, stars(), rating, content,
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
}
