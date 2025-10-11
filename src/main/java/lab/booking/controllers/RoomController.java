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
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "Get all rooms", tags = "Rooms")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of rooms returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(
                                    implementation = Room.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string",
                                    example = "Internal server error"
                            )
                    )
            )
    })
    public ResponseEntity<Object> getAllRooms() {
        log.info("Getting all rooms");
        try {
            List<Room> rooms = bookingService.getAllRooms();
            log.info("Retrieved {} rooms", rooms.size());
            return ResponseEntity.ok(rooms);
        } catch (RuntimeException e) {
            log.error("Error getting all rooms", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/{number}")
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
                            schema = @Schema(
                                    type = "string",
                                    example = "Room with number 101 not found"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string",
                                    example = "Internal server error"
                            )
                    )
            )
    })
    public ResponseEntity<Object> getRoomByNumber(@PathVariable Integer number) {
        log.info("Getting room by number {}", number);
        try {
            Room room = bookingService.getRoomByNumber(number);
            log.info("Found room {} of type {}", number, room.getType());
            return ResponseEntity.ok(room);
        } catch (RoomNotFoundException e) {
            log.warn("Room not found: {}", number);
            return ResponseEntity.status(404).body("Room with number " + number + " not found");
        } catch (RuntimeException e) {
            log.error("Error getting room {}", number, e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Find available rooms", tags = "Rooms")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of available rooms",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(
                                    implementation = Room.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date format or date range",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Invalid date format. Use ISO format: 2025-10-31"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Internal server error"
                            )
                    )
            )
    })
    public ResponseEntity<Object> getAvailableRooms(
            @Parameter(description = "Check-in date (2025-10-31)")
            @RequestParam String fromDate,
            @Parameter(description = "Check-out date (2025-10-31)")
            @RequestParam String toDate) {
        log.info("Searching available rooms from {} to {}", fromDate, toDate);
        try {
            LocalDate from = LocalDate.parse(fromDate);
            LocalDate to = LocalDate.parse(toDate);

            if (from.isAfter(to)) {
                log.warn("Invalid date range: from {} is after to {}", from, to);
                return ResponseEntity.badRequest()
                        .body("Check-in date must be before check-out date");
            }

            List<Room> rooms = bookingService.getAvailableRooms(from, to);
            log.info("Found {} available rooms for period {} to {}", rooms.size(), from, to);
            return ResponseEntity.ok(rooms);
        } catch (java.time.format.DateTimeParseException e) {
            log.warn("Invalid date format: from={}, to={}", fromDate, toDate);
            return ResponseEntity.badRequest()
                    .body("Invalid date format. Use ISO format: 2025-10-31");
        } catch (RuntimeException e) {
            log.error("Error searching available rooms", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping
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
                            schema = @Schema(
                                    type = "string",
                                    example = "Room with this number already exists"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Internal server error"
                            )
                    )
            )
    })
    public ResponseEntity<Object> createRoom(@RequestBody CreateRoomRequest request) {
        log.info("Creating room {} of type {}", request.getNumber(), request.getType());
        try {
            Room room = bookingService.createRoom(request.getNumber(), request.getType());
            log.info("Room {} created successfully", request.getNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid room data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid room data: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error creating room {}", request.getNumber(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/{number}")
    @Operation(summary = "Delete room by number", tags = "Rooms")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Room deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Room deleted successfully"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Room not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Room with number 101 not found"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Internal server error"
                            )
                    )
            )
    })
    public ResponseEntity<String> deleteRoom(@PathVariable Integer number) {
        log.info("Deleting room {}", number);
        try {
            bookingService.deleteRoom(number);
            log.info("Room {} deleted successfully", number);
            return ResponseEntity.ok("Room deleted successfully");
        } catch (RoomNotFoundException e) {
            log.warn("Room not found for deletion: {}", number);
            return ResponseEntity.status(404).body("Room with number " + number + " not found");
        } catch (RuntimeException e) {
            log.error("Error deleting room {}", number, e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // ================= REQUEST DTOs =================

    @Setter
    @Getter
    @Schema(description = "Request to create a new room")
    public static class CreateRoomRequest {
        @Schema(description = "Room number", example = "101")
        private Integer number;

        @Schema(description = "Room type", example = "ECONOMY")
        private RoomType type;
    }
}
