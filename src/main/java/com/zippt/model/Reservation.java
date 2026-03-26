package com.zippt.model;

import com.zippt.enums.ReservationStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reservation {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private long id;
    private long buyerId;
    private long agentId;
    private long propertyId;
    private ReservationStatus status;
    private LocalDateTime reservationDateTime;
    private LocalDateTime createdAt;

    public Reservation() {
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getBuyerId() { return buyerId; }
    public void setBuyerId(long buyerId) { this.buyerId = buyerId; }

    public long getAgentId() { return agentId; }
    public void setAgentId(long agentId) { this.agentId = agentId; }

    public long getPropertyId() { return propertyId; }
    public void setPropertyId(long propertyId) { this.propertyId = propertyId; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getReservationDateTime() { return reservationDateTime; }
    public void setReservationDateTime(LocalDateTime reservationDateTime) {
        this.reservationDateTime = reservationDateTime;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[예약#%d] 매물#%d | 중개사#%d | 방문일시: %s | 상태: %s",
                id, propertyId, agentId,
                reservationDateTime != null ? reservationDateTime.format(FMT) : "-",
                status.getDisplayName());
    }
}
