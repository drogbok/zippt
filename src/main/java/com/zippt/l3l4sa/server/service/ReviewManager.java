package com.zippt.l3l4sa.server.service;

import com.zippt.l3l4sa.common.command.Commands.RegisterReviewCommand;
import com.zippt.l3l4sa.common.result.Results.ReviewResult;
import com.zippt.l3l4sa.common.validation.DomainValidationException;
import com.zippt.l3l4sa.common.validation.ValidationErrorCode;
import com.zippt.l3l4sa.server.domain.Reservation;
import com.zippt.l3l4sa.server.domain.Review;
import java.util.UUID;

public class ReviewManager {
    private final DataStore store;
    private final AuthenticationManager authenticationManager;

    public ReviewManager(DataStore store, AuthenticationManager authenticationManager) {
        this.store = store;
        this.authenticationManager = authenticationManager;
    }

    public ReviewResult registerReview(RegisterReviewCommand command) {
        authenticationManager.validateBuyer(command.buyerId());
        synchronized (store.lockFor("review:" + command.reservationId())) {
            Reservation reservation = store.findReservation(command.reservationId());
            if (reservation == null || !reservation.canWriteReview()) {
                throw new DomainValidationException(
                        ValidationErrorCode.REVIEW_RESERVATION_INVALID,
                        "Review can be written only for a VISITED reservation."
                );
            }
            boolean exists = store.reviews().stream()
                    .anyMatch(review -> review.getReservationId().equals(command.reservationId()));
            if (exists) {
                throw new DomainValidationException(
                        ValidationErrorCode.REVIEW_DUPLICATE_INVALID,
                        "Only one review can be written for a reservation."
                );
            }
            Review review = new Review(
                    "review-" + UUID.randomUUID(),
                    command.reservationId(),
                    command.buyerId(),
                    reservation.getAgentId(),
                    reservation.getPropertyId(),
                    command.rating(),
                    command.text()
            );
            store.saveReview(review);
            reservation.markReviewed();
            return new ReviewResult(review.getReviewId(), "CREATED");
        }
    }
}

