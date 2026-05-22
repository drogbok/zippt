package com.zippt.l1.usecase.reservation;

import com.zippt.l1.usecase.ValidateUser;

/**
 * [L1] Concrete UC : 예약 확정 (Confirm Reservation).
 * Actors : Agent
 * Include : ValidateUser
 * <p>
 * 상태 전이 PENDING -> CONFIRMED (세부는 Description 필요).
 */
public class ConfirmReservation {
    private ValidateUser validateUser;   // <<include>>
    public void execute() { /* TODO */ }
}
