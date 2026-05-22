package com.zippt.l2.exception;

/**
 * [L2] Alternative A5 : 동시 입찰 충돌 (project_proposal 4.1 동시성 제어).
 * <p>
 * "가장 먼저 도착한 요청 외의 나머지 동시 요청은 자동으로 차단하여
 *  중복 낙찰 사고를 원천 방지한다" 라는 요구사항에 대응.
 * Auction 이 이미 AWARDED 로 전이된 후 도착한 입찰이 감지됐을 때 발생.
 */
public class ConcurrentBidException extends Exception {
    public ConcurrentBidException() {
        super("Another winning bid was accepted first");
    }
}
