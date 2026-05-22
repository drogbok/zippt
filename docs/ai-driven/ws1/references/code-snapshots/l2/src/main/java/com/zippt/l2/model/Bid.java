package com.zippt.l2.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * [L2] Entity : 제출된 입찰서.
 */
public class Bid {

    private final String id;
    private final User agent;
    private final Auction auction;
    private final BidForm form;
    private final LocalDateTime timestamp;

    public Bid(User agent, Auction auction, BidForm form, LocalDateTime timestamp) {
        this.id        = UUID.randomUUID().toString();
        this.agent     = agent;
        this.auction   = auction;
        this.form      = form;
        this.timestamp = timestamp;
    }

    public String getId()               { return id; }
    public User getAgent()              { return agent; }
    public Auction getAuction()         { return auction; }
    public BidForm getForm()            { return form; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
