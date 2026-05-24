package com.zippt.l3l4sa.server.service;

import com.zippt.l3l4sa.server.domain.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataStore {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Property> properties = new LinkedHashMap<>();
    private final Map<String, List<Property>> propertiesByRegion = new LinkedHashMap<>();
    private final Map<String, Auction> auctions = new LinkedHashMap<>();
    private final Map<String, Bid> bids = new LinkedHashMap<>();
    private final Map<String, List<Bid>> bidsByAuction = new LinkedHashMap<>();
    private final Map<String, List<Bid>> bidsByAgent = new LinkedHashMap<>();
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();
    private final Map<String, Review> reviews = new LinkedHashMap<>();
    private final Map<String, AgentCredential> credentials = new LinkedHashMap<>();
    private final List<Notification> notifications = new ArrayList<>();
    private final List<OperationLog> operationLogs = new ArrayList<>();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public Object lockFor(String key) {
        return locks.computeIfAbsent(key, ignored -> new Object());
    }

    public void saveUser(User user) { users.put(user.getUserId(), user); }
    public User findUser(String userId) { return users.get(userId); }
    public Collection<User> users() { return users.values(); }
    public void saveProperty(Property property) {
        properties.put(property.getPropertyId(), property);
        // SA NFR-P1: 반복 검색에서 전체 매물 순회를 줄이기 위해 지역별 조회 인덱스를 함께 유지한다.
        propertiesByRegion.computeIfAbsent(property.getRegion(), ignored -> new ArrayList<>()).add(property);
    }
    public Property findProperty(String propertyId) { return properties.get(propertyId); }
    public Collection<Property> properties() { return properties.values(); }
    public List<String> findPropertyIdsByRegion(String region) {
        return propertiesByRegion.getOrDefault(region, Collections.emptyList()).stream()
                .map(Property::getPropertyId)
                .collect(Collectors.toList());
    }
    public void saveAuction(Auction auction) { auctions.put(auction.getAuctionId(), auction); }
    public Auction findAuction(String auctionId) { return auctions.get(auctionId); }
    public Collection<Auction> auctions() { return auctions.values(); }
    public void saveBid(Bid bid) {
        bids.put(bid.getBidId(), bid);
        // SA NFR-P2: 경매/중개사별 입찰 조회가 전체 Bid 목록 순회에 의존하지 않도록 보조 인덱스를 유지한다.
        bidsByAuction.computeIfAbsent(bid.getAuctionId(), ignored -> new ArrayList<>()).add(bid);
        bidsByAgent.computeIfAbsent(bid.getAgentId(), ignored -> new ArrayList<>()).add(bid);
    }
    public Bid findBid(String bidId) { return bids.get(bidId); }
    public Collection<Bid> bids() { return bids.values(); }
    public List<Bid> findBidsByAuction(String auctionId) {
        return new ArrayList<>(bidsByAuction.getOrDefault(auctionId, Collections.emptyList()));
    }
    public List<Bid> findBidsByAgent(String agentId) {
        return new ArrayList<>(bidsByAgent.getOrDefault(agentId, Collections.emptyList()));
    }
    public void saveReservation(Reservation reservation) { reservations.put(reservation.getReservationId(), reservation); }
    public Reservation findReservation(String reservationId) { return reservations.get(reservationId); }
    public Collection<Reservation> reservations() { return reservations.values(); }
    public void saveReview(Review review) { reviews.put(review.getReviewId(), review); }
    public Collection<Review> reviews() { return reviews.values(); }
    public void saveCredential(AgentCredential credential) { credentials.put(credential.getAgentId(), credential); }
    public AgentCredential findCredentialByAgent(String agentId) { return credentials.get(agentId); }
    public void addNotification(Notification notification) { notifications.add(notification); }
    public List<Notification> notifications() { return notifications; }
    public void addOperationLog(OperationLog log) { operationLogs.add(log); }
    public List<OperationLog> operationLogs() { return operationLogs; }
}

