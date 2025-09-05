package com.example.booking;

import com.example.booking.models.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Server {
    private static Server instance;

    private final List<User> users;
    private final List<Room> rooms;
    private final List<Reservation> reservations;

    private Server() {
        this.users = new ArrayList<>();
        this.rooms = new ArrayList<>();
        this.reservations = new ArrayList<>();
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
            instance.migrateStockData();
        }
        return instance;
    }

    private void migrateStockData() {
        User steve = new User("Steven", false);
        User ann = new User("Ann", false);
        User donald = new User("Donald", true);

        instance.addUser(steve);
        instance.addUser(ann);
        instance.addUser(donald);

        Room room10 = new Room(10, Room.RoomType.ECONOMY);
        Room room11 = new Room(11, Room.RoomType.LUX);
        Room room12 = new Room(12, Room.RoomType.PRESIDENTIAL);

        instance.addRoom(room10);
        instance.addRoom(room11);
        instance.addRoom(room12);

        instance.addReservation(new Reservation(steve.getId(),
                                room10.getNumber(),
                                LocalDate.of(2025, 9, 5),
                                LocalDate.of(2025, 9, 12)
                                ));
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addRoom(Room room) throws IllegalArgumentException {
        if (containsRoomWithId(room.getNumber())) {
            throw new IllegalArgumentException("Room with number " + room.getNumber() + " already exists.");
        }
        rooms.add(room);
    }

    public void addReservation(Reservation reservation) throws IllegalArgumentException {
        if (!containsUserWithId(reservation.getUserId())) {
            throw new IllegalArgumentException("User with ID " + reservation.getUserId() + " does not exist.");
        }
        if (!containsRoomWithId(reservation.getRoomId())) {
            throw new IllegalArgumentException("Room with number " + reservation.getRoomId() + " does not exist.");
        }
        if (reservation.getFrom().isAfter(reservation.getTo())) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        if (reservation.getFrom().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        if (isRoomOccupied(reservation.getRoomId(), reservation.getFrom(), reservation.getTo())) {
            throw new IllegalArgumentException("Room " + reservation.getRoomId() +
                    " is already occupied from " + reservation.getFrom() + " to " + reservation.getTo());
        }

        reservations.add(reservation);
    }

    private boolean isRoomOccupied(int roomId, LocalDate from, LocalDate to) {
        for (Reservation existingReservation : reservations) {
            if (existingReservation.getRoomId() == roomId) {
                if (from.isBefore(existingReservation.getTo()) &&
                        to.isAfter(existingReservation.getFrom())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteUser(User user) {
        users.remove(user);
    }

    public void deleteRoom(Room room) {
        rooms.remove(room);
    }

    public void deleteReservation(Reservation reservation) {
        reservations.remove(reservation);
    }

    public boolean containsUserWithId(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) return true;
        }
        return false;
    }

    public boolean containsRoomWithId(int id) {
        for (Room room : rooms) {
            if (room.getNumber() == id) return true;
        }
        return false;
    }

    public boolean containsReservationWithId(String id) {
        for (Reservation reservation : reservations) {
            if (reservation.getId().equals(id)) return true;
        }
        return false;
    }
}
