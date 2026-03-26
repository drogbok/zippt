package com.zippt.model;

import com.zippt.enums.AuctionStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Auction {
    private long id;
    private long propertyId;
    private long sellerId;
    private AuctionStatus status;
    private String requirements;
    private Long awardedBidId;
    private LocalDateTime createdAt;

    public Auction() {
        this.status = AuctionStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPropertyId() { return propertyId; }
    public void setPropertyId(long propertyId) { this.propertyId = propertyId; }

    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public Long getAwardedBidId() { return awardedBidId; }
    public void setAwardedBidId(Long awardedBidId) { this.awardedBidId = awardedBidId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[경매#%d] 매물#%d | 상태: %s | 요구사항: %s | %s",
                id, propertyId, status.getDisplayName(),
                requirements != null ? requirements : "-",
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
}
