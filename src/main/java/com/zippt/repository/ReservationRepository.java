package com.zippt.repository;

import com.zippt.enums.ReservationStatus;
import com.zippt.model.Reservation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ReservationRepository {
    private final Map<Long, Reservation> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Reservation save(Reservation reservation) {
        if (reservation.getId() == 0) {
            reservation.setId(idGen.getAndIncrement());
        }
        store.put(reservation.getId(), reservation);
        return reservation;
    }

    public Optional<Reservation> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Reservation> findByBuyerId(long buyerId) {
        return store.values().stream()
                .filter(r -> r.getBuyerId() == buyerId)
                .collect(Collectors.toList());
    }

    public List<Reservation> findByAgentId(long agentId) {
        return store.values().stream()
                .filter(r -> r.getAgentId() == agentId)
                .collect(Collectors.toList());
    }

    public List<Reservation> findByAgentIdAndStatus(long agentId, ReservationStatus status) {
        return store.values().stream()
                .filter(r -> r.getAgentId() == agentId && r.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(store.values());
    }
}
