package com.zippt.l1.enums;

/**
 * [L1] 역경매 입찰 상태 생애주기.
 * <p>
 * project_proposal.md 4.1 State Transition Model.
 * <pre>
 *   OPEN -> ACTIVE -> AWARDED -> COMPLETED / CANCELLED
 * </pre>
 */
public enum AuctionStatus {
    OPEN,
    ACTIVE,
    AWARDED,
    COMPLETED,
    CANCELLED
}
