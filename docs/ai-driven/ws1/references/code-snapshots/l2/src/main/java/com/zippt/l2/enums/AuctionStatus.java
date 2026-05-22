package com.zippt.l2.enums;

/**
 * [L2] 역경매 상태.
 * <p>
 * project_proposal.md 4.1 State Transition Model 에 따라 정의.
 * Submit Bid 에서 다루는 유효 상태는 OPEN(최초), ACTIVE(입찰 진행 중).
 * AWARDED/COMPLETED/CANCELLED 는 추가 입찰 불가 상태로 A1 으로 처리.
 */
public enum AuctionStatus {
    OPEN,
    ACTIVE,
    AWARDED,
    COMPLETED,
    CANCELLED
}
