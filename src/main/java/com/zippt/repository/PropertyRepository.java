package com.zippt.repository;

import com.zippt.model.Property;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PropertyRepository {
    private final Map<Long, Property> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Property save(Property property) {
        if (property.getId() == 0) {
            property.setId(idGen.getAndIncrement());
        }
        store.put(property.getId(), property);
        return property;
    }

    public Optional<Property> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Property> findBySellerId(long sellerId) {
        return store.values().stream()
                .filter(p -> p.getSellerId() == sellerId)
                .collect(Collectors.toList());
    }

    public List<Property> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean delete(long id) {
        return store.remove(id) != null;
    }
}
