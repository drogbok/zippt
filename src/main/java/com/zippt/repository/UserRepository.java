package com.zippt.repository;

import com.zippt.enums.Role;
import com.zippt.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class UserRepository {
    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public User save(User user) {
        if (user.getId() == 0) {
            user.setId(idGen.getAndIncrement());
        }
        store.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return store.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public List<User> findByRole(Role role) {
        return store.values().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean existsByUsername(String username) {
        return store.values().stream()
                .anyMatch(u -> u.getUsername().equals(username));
    }
}
