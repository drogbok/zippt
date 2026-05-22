package com.zippt.l1.usecase.reservation;

/**
 * [L1] Concrete UC : 후기 작성 (Write Review).
 * Actors : Buyer
 * Include : CompleteVisit (방문 완료 건에만 후기 작성 가능)
 * <p>
 * 상태 전이 VISITED -> REVIEWED.
 */
public class WriteReview {
    private CompleteVisit completeVisit;   // <<include>>
    public void execute() { /* TODO */ }
}
