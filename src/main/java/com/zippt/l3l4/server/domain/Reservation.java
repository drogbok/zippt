package com.zippt.l3l4.server.domain;

import com.zippt.l3l4.common.enums.ReservationStatus;
import java.time.LocalDateTime;

public class Reservation {
    private final String reservationId;
    private final String buyerId;
    private final String propertyId;
    private final String agentId;
    private final LocalDateTime requestedAt;
    private final LocalDateTime visitAt;
    private ReservationStatus status;

    public Reservation(String reservationId, String buyerId, String propertyId, String agentId, LocalDateTime visitAt) {
        this.reservationId = reservationId;
        this.buyerId = buyerId;
        this.propertyId = propertyId;
        this.agentId = agentId;
        this.visitAt = visitAt;
        this.requestedAt = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
    }

    public void confirm() { status = ReservationStatus.CONFIRMED; }
    public void reject() { status = ReservationStatus.REJECTED; }
    public void markVisited() { status = ReservationStatus.VISITED; }
    public void markReviewed() { status = ReservationStatus.REVIEWED; }
    public void cancel() { status = ReservationStatus.CANCELLED; }
    public boolean canWriteReview() { return status == ReservationStatus.VISITED; }

    public String getReservationId() { return reservationId; }
    public String getBuyerId() { return buyerId; }
    public String getPropertyId() { return propertyId; }
    public String getAgentId() { return agentId; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getVisitAt() { return visitAt; }
    public ReservationStatus getStatus() { return status; }
}

