package com.example.booking.models;

import com.example.booking.enums.RoomType;
import lombok.Data;

@Data
public class Room {
    private final int number;
    private RoomType type;

    public Room(int number, RoomType type) {
        this.number = number;
        this.type = type;
    }
}
