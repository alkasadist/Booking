package com.example.booking;

import com.example.booking.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    private Server server;

    @BeforeEach
    void setUp() throws Exception {
        Field instanceField = Server.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        server = Server.getInstance();
    }

    @Test
    @DisplayName("Singleton pattern works correctly")
    void testSingletonPattern() {
        Server server1 = Server.getInstance();
        Server server2 = Server.getInstance();

        assertSame(server1, server2, "getInstance() should return the same instance");
    }

    @Test
    @DisplayName("Initial data is migrated correctly")
    void testMigrateStockData() {
        List<User> users = server.getUsers();
        List<Room> rooms = server.getRooms();
        List<Reservation> reservations = server.getReservations();

        assertEquals(3, users.size(), "Should have 3 users");
        assertEquals(3, rooms.size(), "Should have 3 rooms");
        assertEquals(1, reservations.size(), "Should have 1 reservation");

        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Steven")));
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Ann")));
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Donald")));

        assertTrue(rooms.stream().anyMatch(r -> r.getNumber() == 10));
        assertTrue(rooms.stream().anyMatch(r -> r.getNumber() == 11));
        assertTrue(rooms.stream().anyMatch(r -> r.getNumber() == 12));
    }

    @Test
    @DisplayName("Add user successfully")
    void testAddUser() {
        User newUser = new User("John", false);
        int initialSize = server.getUsers().size();

        server.addUser(newUser);

        assertEquals(initialSize + 1, server.getUsers().size());
        assertTrue(server.containsUserWithId(newUser.getId()));
    }

    @Test
    @DisplayName("Add room successfully")
    void testAddRoom() {
        Room newRoom = new Room(20, Room.RoomType.ECONOMY);
        int initialSize = server.getRooms().size();

        server.addRoom(newRoom);

        assertEquals(initialSize + 1, server.getRooms().size());
        assertTrue(server.containsRoomWithId(20));
    }

    @Test
    @DisplayName("Add room with existing number throws exception")
    void testAddDuplicateRoom() {
        Room duplicateRoom = new Room(10, Room.RoomType.LUX);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> server.addRoom(duplicateRoom)
        );

        assertTrue(exception.getMessage().contains("Room with number 10 already exists"));
    }

    @Test
    @DisplayName("Add reservation successfully")
    void testAddReservation() {
        User user = server.getUsers().getFirst();
        Room room = server.getRooms().stream()
                .filter(r -> r.getNumber() != 10) // room 10 is taken already
                .findFirst()
                .orElseThrow();

        Reservation reservation = new Reservation(
                user.getId(),
                room.getNumber(),
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5)
        );

        int initialSize = server.getReservations().size();
        server.addReservation(reservation);

        assertEquals(initialSize + 1, server.getReservations().size());
        assertTrue(server.containsReservationWithId(reservation.getId()));
    }

    @Test
    @DisplayName("Add reservation with non-existing user throws exception")
    void testAddReservationWithInvalidUser() {
        Reservation reservation = new Reservation(
                "non-existing-user-id",
                11,
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> server.addReservation(reservation)
        );
    }

    @Test
    @DisplayName("Add reservation with non-existing room throws exception")
    void testAddReservationWithInvalidRoom() {
        User user = server.getUsers().getFirst();

        Reservation reservation = new Reservation(
                user.getId(),
                999, // non-existent room
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> server.addReservation(reservation)
        );

        assertTrue(exception.getMessage().contains("Room with number 999 does not exist"));
    }

    @Test
    @DisplayName("Add reservation with invalid dates throws exception")
    void testAddReservationWithInvalidDates() {
        User user = server.getUsers().getFirst();

        Reservation reservation = new Reservation(
                user.getId(),
                11,
                LocalDate.of(2025, 10, 5), // more than 'to' date
                LocalDate.of(2025, 10, 1)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> server.addReservation(reservation)
        );

        assertTrue(exception.getMessage().contains("Start date cannot be after end date"));
    }

    @Test
    @DisplayName("Add reservation with overlapping dates throws exception")
    void testAddReservationWithOverlappingDates() {
        User user = server.getUsers().get(1);

        Reservation overlappingReservation = new Reservation(
                user.getId(),
                10,
                LocalDate.of(2025, 9, 8),
                LocalDate.of(2025, 9, 15)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> server.addReservation(overlappingReservation)
        );

        assertTrue(exception.getMessage().contains("Room 10 is already occupied"));
    }

    @Test
    @DisplayName("Add reservation with non-overlapping dates succeeds")
    void testAddReservationWithNonOverlappingDates() {
        User user = server.getUsers().get(1);

        Reservation reservation = new Reservation(
                user.getId(),
                10,
                LocalDate.of(2025, 9, 13),
                LocalDate.of(2025, 9, 20)
        );

        assertDoesNotThrow(() -> server.addReservation(reservation));
        assertTrue(server.containsReservationWithId(reservation.getId()));
    }

    @Test
    @DisplayName("Delete user successfully")
    void testDeleteUser() {
        User userToDelete = server.getUsers().getFirst();
        int initialSize = server.getUsers().size();

        server.deleteUser(userToDelete);

        assertEquals(initialSize - 1, server.getUsers().size());
        assertFalse(server.containsUserWithId(userToDelete.getId()));
    }

    @Test
    @DisplayName("Delete room successfully")
    void testDeleteRoom() {
        Room roomToDelete = server.getRooms().getFirst();
        int initialSize = server.getRooms().size();

        server.deleteRoom(roomToDelete);

        assertEquals(initialSize - 1, server.getRooms().size());
        assertFalse(server.containsRoomWithId(roomToDelete.getNumber()));
    }

    @Test
    @DisplayName("Delete reservation successfully")
    void testDeleteReservation() {
        Reservation reservationToDelete = server.getReservations().getFirst();
        int initialSize = server.getReservations().size();

        server.deleteReservation(reservationToDelete);

        assertEquals(initialSize - 1, server.getReservations().size());
        assertFalse(server.containsReservationWithId(reservationToDelete.getId()));
    }

    @Test
    @DisplayName("Delete non-existing user does nothing")
    void testDeleteNonExistingUser() {
        User nonExistingUser = new User("Non-existing", false);
        int initialSize = server.getUsers().size();

        server.deleteUser(nonExistingUser);

        assertEquals(initialSize, server.getUsers().size());
    }

    @Test
    @DisplayName("Contains methods work correctly")
    void testContainsMethods() {
        User user = server.getUsers().getFirst();
        Room room = server.getRooms().getFirst();
        Reservation reservation = server.getReservations().getFirst();

        assertTrue(server.containsUserWithId(user.getId()));
        assertTrue(server.containsRoomWithId(room.getNumber()));
        assertTrue(server.containsReservationWithId(reservation.getId()));

        assertFalse(server.containsUserWithId("non-existing-id"));
        assertFalse(server.containsRoomWithId(999));
        assertFalse(server.containsReservationWithId("non-existing-reservation-id"));
    }

    @Test
    @DisplayName("Get methods return unmodifiable lists")
    void testUnmodifiableLists() {
        List<User> users = server.getUsers();
        List<Room> rooms = server.getRooms();
        List<Reservation> reservations = server.getReservations();

        assertThrows(UnsupportedOperationException.class,
                () -> users.add(new User("Test", false)));
        assertThrows(UnsupportedOperationException.class,
                () -> rooms.add(new Room(999, Room.RoomType.ECONOMY)));
        assertThrows(UnsupportedOperationException.class,
                () -> reservations.clear());
    }

    @Test
    @DisplayName("Room occupation logic - 1 day overlap")
    void testRoomOccupationEdgeCases() {
        User user1 = server.getUsers().get(0);
        User user2 = server.getUsers().get(1);
        // current reservation is 5-12 september, room 10

        // new reservation starts the same day previous one ends
        Reservation afterReservation = new Reservation(
                user2.getId(), 10,
                LocalDate.of(2025, 9, 12),
                LocalDate.of(2025, 9, 15)
        );
        assertDoesNotThrow(() -> server.addReservation(afterReservation));
    }

    @Test
    @DisplayName("Multiple overlapping reservations detection")
    void testMultipleOverlappingReservations() {
        User user = server.getUsers().get(1);

        // overlapping reservation
        Reservation reservation1 = new Reservation(
                user.getId(), 10,
                LocalDate.of(2025, 9, 20),
                LocalDate.of(2025, 9, 25)
        );
        server.addReservation(reservation1);

        // another overlapping reservation
        Reservation overlapping = new Reservation(
                user.getId(), 10,
                LocalDate.of(2025, 9, 22),
                LocalDate.of(2025, 9, 27)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> server.addReservation(overlapping)
        );
        assertTrue(exception.getMessage().contains("already occupied"));
    }

    @Test
    @DisplayName("Same day reservation")
    void testSameDayReservation() {
        User user = server.getUsers().get(1);
        LocalDate sameDay = LocalDate.of(2025, 10, 15);

        Reservation sameDayReservation = new Reservation(
                user.getId(), 11,
                sameDay, sameDay
        );

        assertDoesNotThrow(() -> server.addReservation(sameDayReservation));
        assertTrue(server.containsReservationWithId(sameDayReservation.getId()));
    }

    @Test
    @DisplayName("Reservation with null checks")
    void testReservationNullSafety() {
        assertThrows(IllegalArgumentException.class, () -> {
            server.addReservation(new Reservation(
                    null, 11,
                    LocalDate.of(2025, 10, 1),
                    LocalDate.of(2025, 10, 5)
            ));
        });
    }

    @Test
    @DisplayName("Contains methods with null and empty values")
    void testContainsMethodsEdgeCases() {
        assertFalse(server.containsUserWithId(null));
        assertFalse(server.containsUserWithId(""));
        assertFalse(server.containsUserWithId("   "));

        assertFalse(server.containsRoomWithId(-1));
        assertFalse(server.containsRoomWithId(0));

        assertFalse(server.containsReservationWithId(null));
        assertFalse(server.containsReservationWithId(""));
    }

    @Test
    @DisplayName("Large number of reservations performance")
    void testManyReservationsPerformance() {
        User user = server.getUsers().getFirst();

        for (int i = 100; i < 200; i++) {
            server.addRoom(new Room(i, Room.RoomType.ECONOMY));
        }

        long startTime = System.currentTimeMillis();
        for (int i = 100; i < 150; i++) {
            server.addReservation(new Reservation(
                    user.getId(), i,
                    LocalDate.of(2025, 10, 1),
                    LocalDate.of(2025, 10, 5)
            ));
        }
        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime < 1000, "Adding 50 reservations should be fast");
        assertEquals(51, server.getReservations().size());
    }

    @Test
    @DisplayName("Room types validation in migration")
    void testRoomTypesInMigration() {
        List<Room> rooms = server.getRooms();

        Room economyRoom = rooms.stream()
                .filter(r -> r.getNumber() == 10)
                .findFirst()
                .orElseThrow();
        assertEquals(Room.RoomType.ECONOMY, economyRoom.getType());

        Room luxRoom = rooms.stream()
                .filter(r -> r.getNumber() == 11)
                .findFirst()
                .orElseThrow();
        assertEquals(Room.RoomType.LUX, luxRoom.getType());

        Room presidentialRoom = rooms.stream()
                .filter(r -> r.getNumber() == 12)
                .findFirst()
                .orElseThrow();
        assertEquals(Room.RoomType.PRESIDENTIAL, presidentialRoom.getType());
    }

    @Test
    @DisplayName("Admin users validation in migration")
    void testAdminUsersInMigration() {
        List<User> users = server.getUsers();

        User donald = users.stream()
                .filter(u -> u.getName().equals("Donald"))
                .findFirst()
                .orElseThrow();
        assertTrue(donald.isAdmin(), "Donald should be admin");

        User steven = users.stream()
                .filter(u -> u.getName().equals("Steven"))
                .findFirst()
                .orElseThrow();
        assertFalse(steven.isAdmin(), "Steven should not be admin");

        User ann = users.stream()
                .filter(u -> u.getName().equals("Ann"))
                .findFirst()
                .orElseThrow();
        assertFalse(ann.isAdmin(), "Ann should not be admin");
    }

    @Test
    @DisplayName("Reservation date is in the past")
    void testReservationDateBoundaries() {
        User user = server.getUsers().getFirst();

        Reservation pastReservation = new Reservation(
                user.getId(), 11,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 1, 5)
        );
        assertThrows(IllegalArgumentException.class, () -> server.addReservation(pastReservation));
    }

    @Test
    @DisplayName("Data consistency after operations")
    void testDataConsistency() {
        int initialUsers = server.getUsers().size();
        int initialRooms = server.getRooms().size();
        int initialReservations = server.getReservations().size();

        User newUser = new User("TestUser", false);
        Room newRoom = new Room(999, Room.RoomType.LUX);

        server.addUser(newUser);
        server.addRoom(newRoom);

        Reservation newReservation = new Reservation(
                newUser.getId(), 999,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5)
        );
        server.addReservation(newReservation);

        assertEquals(initialUsers + 1, server.getUsers().size());
        assertEquals(initialRooms + 1, server.getRooms().size());
        assertEquals(initialReservations + 1, server.getReservations().size());

        server.deleteReservation(newReservation);
        server.deleteUser(newUser);
        server.deleteRoom(newRoom);

        assertEquals(initialUsers, server.getUsers().size());
        assertEquals(initialRooms, server.getRooms().size());
        assertEquals(initialReservations, server.getReservations().size());
    }
}
