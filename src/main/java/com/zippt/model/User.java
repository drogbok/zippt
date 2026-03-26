package com.zippt.model;

import com.zippt.enums.Role;

public class User {
    private long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private Role role;
    private String region;

    public User() {}

    public User(String username, String password, String name, String phone, Role role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    @Override
    public String toString() {
        String base = String.format("[ID:%d] %s (%s) | %s | %s",
                id, name, username, role.getDisplayName(), phone);
        if (role == Role.AGENT && region != null) {
            base += " | 담당지역: " + region;
        }
        return base;
    }
}
