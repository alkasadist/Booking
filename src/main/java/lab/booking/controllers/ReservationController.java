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
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReservationController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "Get all reservations", tags = "Reservations")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of reservations returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(
                                    implementation = Reservation.class)
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
    public ResponseEntity<Object> getAllReservations() {
        try {
            return ResponseEntity.ok(bookingService.getAllReservations());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user reservations", tags = "Reservations")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User reservations returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = Reservation.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "User with id 123 not found"
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

    @PostMapping
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
                            schema = @Schema(
                                    type = "string",
                                    example = "Room is not available for the selected dates"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or room not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "User with id 123 not found"
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
    public ResponseEntity<Object> createReservation(@RequestBody CreateReservationRequest request) {
        try {
            LocalDate from = LocalDate.parse(request.getFromDate());
            LocalDate to = LocalDate.parse(request.getToDate());

            if (from.isAfter(to)) {
                return ResponseEntity.badRequest()
                        .body("Check-in date must be before check-out date");
            }

            Reservation reservation = bookingService.createReservation(
                    request.getGuestId(),
                    request.getRoomNumber(),
                    from,
                    to
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404)
                    .body("User with id " + request.getGuestId() + " not found");
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(404)
                    .body("Room with number " + request.getRoomNumber() + " not found");
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid date format. Use ISO format: 2025-10-01");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid reservation data: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body("Internal server error");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel reservation", tags = "Reservations")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation cancelled successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Reservation cancelled successfully"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string",
                                    example = "Reservation with id 123 not found"
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

    // ================= REQUEST DTOs =================

    @Setter
    @Getter
    @Schema(description = "Request to create a new reservation")
    public static class CreateReservationRequest {
        @Schema(description = "ID of the guest making the reservation", example = "1")
        private Integer guestId;

        @Schema(description = "Number of the room to reserve", example = "101")
        private Integer roomNumber;

        @Schema(description = "Check-in date and time", example = "2025-10-31")
        private String fromDate;

        @Schema(description = "Check-out date and time", example = "2025-10-31")
        private String toDate;
    }
}
