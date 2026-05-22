package com.zippt.l2.model;

/**
 * [L2] 사용자(=Agent).
 * <p>
 * Precondition P1 / P3 를 상태 쿼리로 노출.
 */
public class User {

    private final String id;
    private final String name;
    private boolean authenticated;   // P1 : 로그인 세션 유효
    private boolean certified;       // P3 : 공인중개사 자격 검증 완료

    public User(String id, String name, boolean authenticated, boolean certified) {
        this.id = id;
        this.name = name;
        this.authenticated = authenticated;
        this.certified = certified;
    }

    public String getId()            { return id; }
    public String getName()          { return name; }
    public boolean isAuthenticated() { return authenticated; }
    public boolean isCertified()     { return certified; }
}
