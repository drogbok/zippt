package com.zippt.l1.usecase.account;

import com.zippt.l1.usecase.ValidateUser;

/**
 * [L1] Concrete UC : 시스템 로그인 (Login System).
 * Actors : Buyer, Seller, Agent  (3인 공통)
 * Include : ValidateUser
 */
public class LoginSystem {
    private ValidateUser validateUser;   // <<include>>
    public void execute() { /* TODO */ }
}
