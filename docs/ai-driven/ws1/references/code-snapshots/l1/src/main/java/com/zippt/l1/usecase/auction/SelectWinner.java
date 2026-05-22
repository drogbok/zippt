package com.zippt.l1.usecase.auction;

import com.zippt.l1.usecase.ValidateUser;

/**
 * [L1] Concrete UC : 낙찰자 선정 (Select Winner).
 * Actors : Seller
 * Include : ValidateUser
 * Extends : SubmitBid (Base)
 * <p>
 * 상태 전이 ACTIVE -> AWARDED (세부는 Description 필요).
 */
public class SelectWinner {
    private ValidateUser validateUser;   // <<include>>
    private SubmitBid baseUseCase;       // <<extend>> Base
    public void execute() { /* TODO */ }
}
