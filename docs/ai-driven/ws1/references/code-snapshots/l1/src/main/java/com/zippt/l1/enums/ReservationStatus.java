package com.zippt.l1.enums;

/**
 * [L1] 예약 생애주기 상태.
 * <p>
 * project_proposal.md 4.1 State Transition Model.
 * <pre>
 *   PENDING -> CONFIRMED -> VISITED -> REVIEWED
 * </pre>
 */
public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    VISITED,
    REVIEWED
}
