package com.zippt.l2.usecase;

import com.zippt.l2.model.User;
import com.zippt.l2.exception.AuthException;

/**
 * [L2] Abstract Use Case : 사용자 인증 수행.
 * <p>
 * Description 1단계(include)에서 호출되며,
 * Precondition P1 (세션 유효) / P3 (자격 증명 완료) 을 검사.
 */
public class ValidateUser {

    public void execute(User agent) throws AuthException {
        if (agent == null || !agent.isAuthenticated()) {
            throw new AuthException();                 // P1
        }
        if (!agent.isCertified()) {
            throw new AuthException();                 // P3
        }
    }
}
