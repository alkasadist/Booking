package com.example.booking.models;

import lombok.Value;
import java.time.LocalDate;
import java.util.UUID;

@Value
public class Reservation {
    String id;
    String userId;
    int roomId;

    LocalDate from;
    LocalDate to;

    public Reservation(String userId, int roomId, LocalDate from, LocalDate to) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.roomId = roomId;
        this.from = from;
        this.to = to;
    }
}
