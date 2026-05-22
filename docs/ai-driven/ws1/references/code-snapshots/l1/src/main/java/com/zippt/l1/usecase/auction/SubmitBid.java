package com.zippt.l1.usecase.auction;

import com.zippt.l1.usecase.ValidateUser;

/**
 * [L1] Concrete UC : 입찰 참여 (Submit Bid).
 * Actors : Agent
 * Include : ValidateUser
 * Base UC of : SelectWinner (via <<extend>>)
 * <p>
 * project_proposal.md 4.1 : "입찰 동시성 제어" — 상세 로직은 Description 필요.
 */
public class SubmitBid {
    private ValidateUser validateUser;   // <<include>>
    public void execute() { /* TODO: 동시성 제어 로직 Description 필요 */ }
}
