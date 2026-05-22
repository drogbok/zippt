package com.zippt.l3l4.server.service;

import com.zippt.l3l4.server.domain.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Property> properties = new LinkedHashMap<>();
    private final Map<String, Auction> auctions = new LinkedHashMap<>();
    private final Map<String, Bid> bids = new LinkedHashMap<>();
    private final Map<String, AgentCredential> credentials = new LinkedHashMap<>();
    private final List<Notification> notifications = new ArrayList<>();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public Object lockFor(String key) {
        return locks.computeIfAbsent(key, ignored -> new Object());
    }

    public void saveUser(User user) { users.put(user.getUserId(), user); }
    public User findUser(String userId) { return users.get(userId); }
    public Collection<User> users() { return users.values(); }
    public void saveProperty(Property property) { properties.put(property.getPropertyId(), property); }
    public Property findProperty(String propertyId) { return properties.get(propertyId); }
    public Collection<Property> properties() { return properties.values(); }
    public void saveAuction(Auction auction) { auctions.put(auction.getAuctionId(), auction); }
    public Auction findAuction(String auctionId) { return auctions.get(auctionId); }
    public Collection<Auction> auctions() { return auctions.values(); }
    public void saveBid(Bid bid) { bids.put(bid.getBidId(), bid); }
    public Bid findBid(String bidId) { return bids.get(bidId); }
    public Collection<Bid> bids() { return bids.values(); }
    public void saveCredential(AgentCredential credential) { credentials.put(credential.getAgentId(), credential); }
    public AgentCredential findCredentialByAgent(String agentId) { return credentials.get(agentId); }
    public void addNotification(Notification notification) { notifications.add(notification); }
    public List<Notification> notifications() { return notifications; }
}
