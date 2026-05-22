package com.zippt.l3l4.server.service;

import com.zippt.l3l4.common.command.Commands.RegisterReviewCommand;
import com.zippt.l3l4.common.result.Results.ReviewResult;

public class ReviewManager {
    private final AuthenticationManager authenticationManager;

    public ReviewManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public ReviewResult registerReview(RegisterReviewCommand command) {
        authenticationManager.validateBuyer(command.buyerId());
        if (command.rating() < 1 || command.rating() > 5) {
            throw new IllegalArgumentException("Review rating must be between 1 and 5.");
        }
        return new ReviewResult("review-created", "CREATED");
    }
}

