package com.zippt.l3l4sa.server.service;

import com.zippt.l3l4sa.common.command.Commands.RequestVisitCommand;
import com.zippt.l3l4sa.common.result.Results.ReservationResult;
import com.zippt.l3l4sa.common.validation.DataDictionaryValidator;
import com.zippt.l3l4sa.common.validation.DomainValidationException;
import com.zippt.l3l4sa.common.validation.ValidationErrorCode;
import com.zippt.l3l4sa.server.domain.Reservation;
import java.time.Duration;
import java.util.UUID;

public class ReservationManager {
    private final DataStore store;
    private final AuthenticationManager authenticationManager;

    public ReservationManager(DataStore store, AuthenticationManager authenticationManager) {
        this.store = store;
        this.authenticationManager = authenticationManager;
    }

    public ReservationResult requestVisit(RequestVisitCommand command) {
        authenticationManager.validateBuyer(command.buyerId());
        DataDictionaryValidator.requireFutureWithin(
                command.visitAt(), Duration.ZERO, Duration.ofDays(60), ValidationErrorCode.RESERVATION_VISIT_AT_INVALID);

        String lockKey = "reservation:" + command.agentId() + ":" + command.visitAt();
        synchronized (store.lockFor(lockKey)) {
            boolean duplicated = store.reservations().stream()
                    .anyMatch(reservation -> reservation.getAgentId().equals(command.agentId())
                            && reservation.getVisitAt().equals(command.visitAt())
                            && (reservation.getStatus() == com.zippt.l3l4sa.common.enums.ReservationStatus.PENDING
                            || reservation.getStatus() == com.zippt.l3l4sa.common.enums.ReservationStatus.CONFIRMED));
            if (duplicated) {
                throw new DomainValidationException(
                        ValidationErrorCode.RESERVATION_DUPLICATE_INVALID,
                        "Agent already has a pending or confirmed reservation at the requested time."
                );
            }
            Reservation reservation = new Reservation(
                    "reservation-" + UUID.randomUUID(),
                    command.buyerId(),
                    command.propertyId(),
                    command.agentId(),
                    command.visitAt()
            );
            store.saveReservation(reservation);
            return new ReservationResult(reservation.getReservationId(), reservation.getStatus().name());
        }
    }
}

