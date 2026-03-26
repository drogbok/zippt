package com.zippt.service;

import com.zippt.enums.Role;
import com.zippt.model.User;
import com.zippt.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, String name, String phone, Role role, String region) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다: " + username);
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        User user = new User(username, password, name, phone, role);
        if (role == Role.AGENT && region != null) {
            user.setRegion(region);
        }
        return userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
