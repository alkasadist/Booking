package lab.booking.controllers;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lab.booking.exceptions.*;
import lab.booking.models.*;
import lab.booking.enums.*;
import lab.booking.services.BookingService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> getAllUsers() {
        try {
            return ResponseEntity.ok(bookingService.getAllUsers());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", tags = "Users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "User with id 123 not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> getUserById(@PathVariable Integer id) {
        try {
            User user = bookingService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body("User with id " + id + " not found");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/users")
    @Operation(summary = "Create new user", tags = "Users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Invalid user data")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = bookingService.createUser(request.getName(), request.getRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid user data: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user's name", tags = "Users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "User updated successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "User with id 123 not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Invalid user data")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<String> updateUser(@PathVariable Integer id,
                                             @RequestBody UpdateUserRequest request) {
        try {
            bookingService.updateUserName(id, request.getName());
            return ResponseEntity.ok("User updated successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body("User with id " + id + " not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid user data: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user by ID", tags = "Users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "User deleted successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "User with id 123 not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        try {
            bookingService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body("User with id " + id + " not found");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // ================= ROOM ENDPOINTS =================

    @GetMapping("/rooms")
    @Operation(summary = "Get all rooms", tags = "Rooms")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of rooms returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Room.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> getAllRooms() {
        try {
            return ResponseEntity.ok(bookingService.getAllRooms());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/rooms/{number}")
    @Operation(summary = "Get room by number", tags = "Rooms")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Room found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Room.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Room not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Room with number 101 not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> getRoomByNumber(@PathVariable Integer number) {
        try {
            Room room = bookingService.getRoomByNumber(number);
            return ResponseEntity.ok(room);
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(404).body("Room with number " + number + " not found");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/rooms/available")
    @Operation(summary = "Find available rooms", tags = "Rooms")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of available rooms",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Room.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date format or date range",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Invalid date format. Use ISO format: 2025-10-01T15:00:00")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> getAvailableRooms(
            @Parameter(description = "Check-in date (2025-10-01T15:00:00)")
            @RequestParam String fromDate,
            @Parameter(description = "Check-out date (2025-10-05T11:00:00)")
            @RequestParam String toDate) {
        try {
            LocalDateTime from = LocalDateTime.parse(fromDate);
            LocalDateTime to = LocalDateTime.parse(toDate);

            if (from.isAfter(to)) {
                return ResponseEntity.badRequest().body("Check-in date must be before check-out date");
            }

            List<Room> rooms = bookingService.getAvailableRooms(from, to);
            return ResponseEntity.ok(rooms);
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use ISO format: 2025-10-01T15:00:00");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/rooms")
    @Operation(summary = "Add new room", tags = "Rooms")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Room created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Room.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or room already exists",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Room with this number already exists")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> createRoom(@RequestBody CreateRoomRequest request) {
        try {
            Room room = bookingService.createRoom(request.getNumber(), request.getType());
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid room data: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/rooms/{number}")
    @Operation(summary = "Delete room by number", tags = "Rooms")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Room deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Room deleted successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Room not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Room with number 101 not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<String> deleteRoom(@PathVariable Integer number) {
        try {
            bookingService.deleteRoom(number);
            return ResponseEntity.ok("Room deleted successfully");
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(404).body("Room with number " + number + " not found");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // ================= RESERVATION ENDPOINTS =================

    @GetMapping("/reservations")
    @Operation(summary = "Get all reservations", tags = "Reservations")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of reservations returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Reservation.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> getAllReservations() {
        try {
            return ResponseEntity.ok(bookingService.getAllReservations());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/users/{userId}/reservations")
    @Operation(summary = "Get user reservations", tags = "Reservations")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User reservations returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Reservation.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "User with id 123 not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> getUserReservations(@PathVariable Integer userId) {
        try {
            List<Reservation> reservations = bookingService.getUserReservations(userId);
            return ResponseEntity.ok(reservations);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body("User with id " + userId + " not found");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/reservations")
    @Operation(summary = "Create new reservation", tags = "Reservations")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reservation.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data, room unavailable, or date conflicts",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Room is not available for the selected dates")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or room not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "User with id 123 not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<Object> createReservation(@RequestBody CreateReservationRequest request) {
        try {
            LocalDateTime from = LocalDateTime.parse(request.getFromDate());
            LocalDateTime to = LocalDateTime.parse(request.getToDate());

            if (from.isAfter(to)) {
                return ResponseEntity.badRequest().body("Check-in date must be before check-out date");
            }

            Reservation reservation = bookingService.createReservation(
                    request.getGuestId(),
                    request.getRoomNumber(),
                    from,
                    to
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body("User with id " + request.getGuestId() + " not found");
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(404).body("Room with number " + request.getRoomNumber() + " not found");
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use ISO format: 2025-10-01T15:00:00");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid reservation data: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/reservations/{id}")
    @Operation(summary = "Cancel reservation", tags = "Reservations")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation cancelled successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Reservation cancelled successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Reservation with id 123 not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Internal server error")
                    )
            )
    })
    public ResponseEntity<String> cancelReservation(@PathVariable Integer id) {
        try {
            bookingService.cancelReservation(id);
            return ResponseEntity.ok("Reservation cancelled successfully");
        } catch (ReservationNotFoundException e) {
            return ResponseEntity.status(404).body("Reservation with id " + id + " not found");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // ================= REQUEST TEMPLATES =================

    @Setter
    @Getter
    @Schema(description = "Request to create a new user")
    public static class CreateUserRequest {
        @Schema(description = "User's name", example = "John Doe")
        private String name;

        @Schema(description = "User's role", example = "GUEST")
        private UserRole role;
    }

    @Setter
    @Getter
    @Schema(description = "Request to update user information")
    public static class UpdateUserRequest {
        @Schema(description = "New user's name", example = "Jane Doe")
        private String name;
    }

    @Setter
    @Getter
    @Schema(description = "Request to create a new room")
    public static class CreateRoomRequest {
        @Schema(description = "Room number", example = "101")
        private Integer number;

        @Schema(description = "Room type", example = "ECONOMY")
        private RoomType type;
    }

    @Setter
    @Getter
    @Schema(description = "Request to create a new reservation")
    public static class CreateReservationRequest {
        @Schema(description = "ID of the guest making the reservation", example = "1")
        private Integer guestId;

        @Schema(description = "Number of the room to reserve", example = "101")
        private Integer roomNumber;

        @Schema(description = "Check-in date and time", example = "2025-10-01T15:00:00")
        private String fromDate;

        @Schema(description = "Check-out date and time", example = "2025-10-05T11:00:00")
        private String toDate;
    }
}
