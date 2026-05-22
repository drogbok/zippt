package com.zippt.l1.model;

import com.zippt.l1.enums.Role;

/**
 * [L1] 도메인 모델 : 사용자.
 * <p>
 * Diagram 수준에서 Actor(Buyer/Seller/Agent)의 공통 부모로 식별되었으나,
 * 속성 구성은 Description 없이는 결정 불가.
 */
public class User {
    private Role role;
    // TODO: id, name, email, 자격 정보 등 Diagram 에 없음
}
