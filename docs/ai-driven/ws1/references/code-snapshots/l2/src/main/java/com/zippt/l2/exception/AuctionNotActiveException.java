package com.zippt.l2.exception;

/**
 * [L2] Alternative A1 : 경매가 AWARDED/COMPLETED/CANCELLED 이거나 마감된 경우.
 */
public class AuctionNotActiveException extends Exception {
    public AuctionNotActiveException() {
        super("Auction does not accept bids");
    }
}
