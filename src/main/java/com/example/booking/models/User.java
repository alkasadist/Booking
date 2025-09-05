package com.example.booking.models;

import lombok.Data;
import java.util.UUID;

@Data
public class User {
    private String id;
    private String name;
    private boolean admin;

    public User(String name, boolean admin) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.admin = admin;
    }
}
