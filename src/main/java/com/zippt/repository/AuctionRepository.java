package com.zippt.repository;

import com.zippt.enums.AuctionStatus;
import com.zippt.model.Auction;
import com.zippt.model.Bid;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class AuctionRepository {
    private final Map<Long, Auction> auctionStore = new ConcurrentHashMap<>();
    private final Map<Long, Bid> bidStore = new ConcurrentHashMap<>();
    private final AtomicLong auctionIdGen = new AtomicLong(1);
    private final AtomicLong bidIdGen = new AtomicLong(1);

    public Auction saveAuction(Auction auction) {
        if (auction.getId() == 0) {
            auction.setId(auctionIdGen.getAndIncrement());
        }
        auctionStore.put(auction.getId(), auction);
        return auction;
    }

    public Bid saveBid(Bid bid) {
        if (bid.getId() == 0) {
            bid.setId(bidIdGen.getAndIncrement());
        }
        bidStore.put(bid.getId(), bid);
        return bid;
    }

    public Optional<Auction> findAuctionById(long id) {
        return Optional.ofNullable(auctionStore.get(id));
    }

    public Optional<Bid> findBidById(long id) {
        return Optional.ofNullable(bidStore.get(id));
    }

    public List<Auction> findAuctionsBySellerId(long sellerId) {
        return auctionStore.values().stream()
                .filter(a -> a.getSellerId() == sellerId)
                .collect(Collectors.toList());
    }

    public List<Auction> findAuctionsByStatus(AuctionStatus status) {
        return auctionStore.values().stream()
                .filter(a -> a.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Auction> findOpenOrActiveAuctions() {
        return auctionStore.values().stream()
                .filter(a -> a.getStatus() == AuctionStatus.OPEN || a.getStatus() == AuctionStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    public List<Bid> findBidsByAuctionId(long auctionId) {
        return bidStore.values().stream()
                .filter(b -> b.getAuctionId() == auctionId)
                .collect(Collectors.toList());
    }

    public List<Bid> findBidsByAgentId(long agentId) {
        return bidStore.values().stream()
                .filter(b -> b.getAgentId() == agentId)
                .collect(Collectors.toList());
    }

    public List<Auction> findAllAuctions() {
        return new ArrayList<>(auctionStore.values());
    }
}
