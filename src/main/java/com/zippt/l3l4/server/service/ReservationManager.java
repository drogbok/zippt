package com.zippt.l3l4.server.service;

import com.zippt.l3l4.common.command.Commands.RequestVisitCommand;
import com.zippt.l3l4.common.result.Results.ReservationResult;

public class ReservationManager {
    private final AuthenticationManager authenticationManager;

    public ReservationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public ReservationResult requestVisit(RequestVisitCommand command) {
        authenticationManager.validateBuyer(command.buyerId());
        // L4 명세의 agentId + visitAt lock 대상이다. 저장 구현은 SA/후속 코드에서 확장한다.
        return new ReservationResult("reservation-pending", "PENDING");
    }
}

