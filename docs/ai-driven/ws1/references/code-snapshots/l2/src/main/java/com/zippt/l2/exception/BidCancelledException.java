package com.zippt.l2.exception;

/**
 * [L2] Alternative A3 : 사용자가 입찰을 취소한 경우.
 */
public class BidCancelledException extends Exception {
    public BidCancelledException() {
        super("Bid submission was cancelled by the user");
    }
}
