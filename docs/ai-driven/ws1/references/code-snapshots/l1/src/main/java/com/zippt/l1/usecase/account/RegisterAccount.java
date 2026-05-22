package com.zippt.l1.usecase.account;

import com.zippt.l1.usecase.ValidateUser;

/**
 * [L1] Concrete UC : 계정 등록 (Register Account).
 * Actors : Buyer, Seller, Agent  (3인 공통)
 * Include : ValidateUser
 */
public class RegisterAccount {
    private ValidateUser validateUser;   // <<include>>
    public void execute() { /* TODO */ }
}
