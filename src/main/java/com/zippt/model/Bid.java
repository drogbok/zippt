package com.zippt.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bid {
    private long id;
    private long auctionId;
    private long agentId;
    private double commissionRate;
    private String conditions;
    private LocalDateTime createdAt;

    public Bid() {
        this.createdAt = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }

    public long getAgentId() { return agentId; }
    public void setAgentId(long agentId) { this.agentId = agentId; }

    public double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(double commissionRate) { this.commissionRate = commissionRate; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[입찰#%d] 경매#%d | 중개사#%d | 수수료: %.2f%% | 조건: %s",
                id, auctionId, agentId, commissionRate,
                conditions != null ? conditions : "-");
    }
}
