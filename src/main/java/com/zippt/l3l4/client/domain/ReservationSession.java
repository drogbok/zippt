package com.zippt.l3l4.client.domain;

import com.zippt.l3l4.common.command.Commands.RequestVisitCommand;
import java.time.LocalDateTime;

public class ReservationSession {
    private String buyerId;
    private String propertyId;
    private String agentId;
    private LocalDateTime visitAt;

    public void storeVisitRequest(String buyerId, String propertyId, String agentId, LocalDateTime visitAt) {
        this.buyerId = buyerId;
        this.propertyId = propertyId;
        this.agentId = agentId;
        this.visitAt = visitAt;
    }

    public RequestVisitCommand toRequestVisitCommand() {
        return new RequestVisitCommand(buyerId, propertyId, agentId, visitAt);
    }

    public void discard() {
        buyerId = null;
        propertyId = null;
        agentId = null;
        visitAt = null;
    }
}

