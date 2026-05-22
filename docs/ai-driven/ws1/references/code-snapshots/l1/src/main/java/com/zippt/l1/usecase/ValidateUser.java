package com.zippt.l1.usecase;

/**
 * [L1] Abstract Use Case : 사용자 인증 수행 (Validate User).
 * <p>
 * Diagram 에서 {@code <<abstract>>} 스테레오타입 표기.
 * RegisterAccount, LoginSystem, ConfirmReservation, SelectWinner, SubmitBid 가 {@code <<include>>}.
 */
public abstract class ValidateUser {
    public abstract void execute();
    // TODO: 인증 로직은 Diagram 에 없음
}
