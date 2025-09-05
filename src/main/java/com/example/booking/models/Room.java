package com.example.booking.models;

import lombok.Data;

@Data
public class Room {
    private final int number;
    public enum RoomType { ECONOMY, LUX, PRESIDENTIAL}
    private RoomType type;

    public Room(int number, RoomType type) {
        this.number = number;
        this.type = type;
    }
}
