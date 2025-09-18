package lab.booking.controllers;

import lab.booking.exceptions.UserNotFoundException;
import lab.booking.models.*;
import lab.booking.services.BookingService;
import lab.booking.enums.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    // ================= USER ENDPOINTS =================

    @GetMapping("/users")
    @Operation(summary = "Get all users", tags = "Users")
    @ApiResponse(responseCode = "200", description = "List of users returned successfully")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(bookingService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", tags = "Users")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        try {
            User user = bookingService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/users")
    @Operation(summary = "Create new user", tags = "Users")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = bookingService.createUser(request.getName(), request.getRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user's name", tags = "Users")
    public ResponseEntity<String> updateUser(@PathVariable Integer id,
                                             @RequestBody UpdateUserRequest request) {
        try {
            bookingService.updateUserName(id, request.getName());
            return ResponseEntity.ok("User updated successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body("User with id " + id + " not found");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user by ID", tags = "Users")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        try {
            bookingService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================= ROOM ENDPOINTS =================

    @GetMapping("/rooms")
    @Operation(summary = "Get all rooms", tags = "Rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(bookingService.getAllRooms());
    }

    @GetMapping("/rooms/{number}")
    @Operation(summary = "Get room by ID", tags = "Rooms")
    public ResponseEntity<Room> getRoomByNumber(@PathVariable Integer number) {
        try {
            Room room = bookingService.getRoomByNumber(number);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/rooms/available")
    @Operation(summary = "Find available rooms", tags = "Rooms")
    @ApiResponse(responseCode = "200", description = "List of available rooms")
    @ApiResponse(responseCode = "400", description = "Incorrect data format")
    public ResponseEntity<List<Room>> getAvailableRooms(
            @Parameter(description = "Check-in date (2025-10-01T15:00:00)")
            @RequestParam String fromDate,
            @Parameter(description = "Check-out date (2025-10-05T11:00:00)")
            @RequestParam String toDate) {
        try {
            LocalDateTime from = LocalDateTime.parse(fromDate);
            LocalDateTime to = LocalDateTime.parse(toDate);
            List<Room> rooms = bookingService.getAvailableRooms(from, to);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/rooms")
    @Operation(summary = "Add new room", tags = "Rooms")
    public ResponseEntity<Room> createRoom(@RequestBody CreateRoomRequest request) {
        try {
            Room room = bookingService.createRoom(request.getNumber(), request.getType());
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/rooms/{number}")
    @Operation(summary = "Delete room by ID", tags = "Rooms")
    public ResponseEntity<String> deleteRoom(@PathVariable Integer number) {
        try {
            bookingService.deleteRoom(number);
            return ResponseEntity.ok("Room deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================= RESERVATION ENDPOINTS =================

    @GetMapping("/reservations")
    @Operation(summary = "Get all reservations", tags = "Reservations")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(bookingService.getAllReservations());
    }

    @GetMapping("/users/{userId}/reservations")
    @Operation(summary = "Get reservations of specific user by his ID", tags = "Reservations")
    public ResponseEntity<List<Reservation>> getUserReservations(@PathVariable Integer userId) {
        try {
            List<Reservation> reservations = bookingService.getUserReservations(userId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/reservations")
    @Operation(summary = "Create new reservation", tags = "Reservations")
    @ApiResponse(responseCode = "200", description = "Reservation created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid data or room is unavailable")
    public ResponseEntity<Reservation> createReservation(@RequestBody CreateReservationRequest request) {
        try {
            LocalDateTime from = LocalDateTime.parse(request.getFromDate());
            LocalDateTime to = LocalDateTime.parse(request.getToDate());

            Reservation reservation = bookingService.createReservation(
                    request.getGuestId(),
                    request.getRoomNumber(),
                    from,
                    to
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/reservations/{id}")
    @Operation(summary = "Delete reservation by ID", tags = "Reservations")
    public ResponseEntity<String> cancelReservation(@PathVariable Integer id) {
        try {
            bookingService.cancelReservation(id);
            return ResponseEntity.ok("Reservation cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================= REQUEST DTOs =================

    @Setter
    @Getter
    public static class CreateUserRequest {
        private String name;
        private UserRole role;
    }

    @Setter
    @Getter
    public static class UpdateUserRequest {
        private String name;
    }

    @Setter
    @Getter
    public static class CreateRoomRequest {
        private Integer number;
        private RoomType type;
    }

    @Setter
    @Getter
    public static class CreateReservationRequest {
        private Integer guestId;
        private Integer roomNumber;
        private String fromDate;
        private String toDate;
    }
}
