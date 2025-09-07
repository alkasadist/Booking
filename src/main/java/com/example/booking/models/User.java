package com.example.booking.models;

import com.example.booking.enums.UserRole;
import lombok.Data;
import java.util.UUID;

@Data
public class User {
    private String id;
    private String name;
    private UserRole role;

    public User(String name, UserRole role) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.role = role;
    }
}
