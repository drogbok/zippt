package com.zippt.repository;

import com.zippt.model.Review;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ReviewRepository {
    private final Map<Long, Review> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Review save(Review review) {
        if (review.getId() == 0) {
            review.setId(idGen.getAndIncrement());
        }
        store.put(review.getId(), review);
        return review;
    }

    public Optional<Review> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Review> findByAgentId(long agentId) {
        return store.values().stream()
                .filter(r -> r.getAgentId() == agentId)
                .collect(Collectors.toList());
    }

    public List<Review> findByPropertyId(long propertyId) {
        return store.values().stream()
                .filter(r -> r.getPropertyId() == propertyId)
                .collect(Collectors.toList());
    }

    public List<Review> findByBuyerId(long buyerId) {
        return store.values().stream()
                .filter(r -> r.getBuyerId() == buyerId)
                .collect(Collectors.toList());
    }

    public boolean existsByReservationId(long reservationId) {
        return store.values().stream()
                .anyMatch(r -> r.getReservationId() == reservationId);
    }

    public List<Review> findAll() {
        return new ArrayList<>(store.values());
    }
}
