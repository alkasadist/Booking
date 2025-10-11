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
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "Get all users", tags = "Users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(
                                    implementation = User.class)
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
    public ResponseEntity<Object> getAllUsers() {
        log.info("Getting all users");
        try {
            List<User> users = bookingService.getAllUsers();
            log.info("Retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            log.error("Error getting all users", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/{id}")
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
                            schema = @Schema(type = "string",
                                    example = "User with id 123 not found"
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
    public ResponseEntity<Object> getUserById(@PathVariable Integer id) {
        log.info("Getting user by ID {}", id);
        try {
            User user = bookingService.getUserById(id);
            log.info("Found user {} with role {}", user.getName(), user.getRole());
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", id);
            return ResponseEntity.status(404).body("User with id " + id + " not found");
        } catch (RuntimeException e) {
            log.error("Error getting user {}", id, e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping
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
                            schema = @Schema(type = "string",
                                    example = "Invalid user data"
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
    public ResponseEntity<Object> createUser(@RequestBody CreateUserRequest request) {
        log.info("Creating user {} with role {}", request.getName(), request.getRole());
        try {
            User user = bookingService.createUser(request.getName(), request.getRole());
            log.info("User created successfully with ID {}", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid user data: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error creating user {}", request.getName(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user's name", tags = "Users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string",
                                    example = "User updated successfully"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string",
                                    example = "User with id 123 not found"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data provided",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string",
                                    example = "Invalid user data"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    type = "string", example = "Internal server error"
                            )
                    )
            )
    })
    public ResponseEntity<String> updateUser(@PathVariable Integer id,
                                             @RequestBody UpdateUserRequest request) {
        log.info("Updating user {} name to {}", id, request.getName());
        try {
            bookingService.updateUserName(id, request.getName());
            log.info("User {} updated successfully", id);
            return ResponseEntity.ok("User updated successfully");
        } catch (UserNotFoundException e) {
            log.warn("User not found for update: {}", id);
            return ResponseEntity.status(404).body("User with id " + id + " not found");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user data for update: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid user data: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error updating user {}", id, e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID", tags = "Users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string",
                                    example = "User deleted successfully"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string",
                                    example = "User with id 123 not found"
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
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        log.info("Deleting user {}", id);
        try {
            bookingService.deleteUser(id);
            log.info("User {} deleted successfully", id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (UserNotFoundException e) {
            log.warn("User not found for deletion: {}", id);
            return ResponseEntity.status(404).body("User with id " + id + " not found");
        } catch (RuntimeException e) {
            log.error("Error deleting user {}", id, e);
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

        @Schema(description = "User's role", example = "USER")
        private UserRole role;
    }

    @Setter
    @Getter
    @Schema(description = "Request to update user information")
    public static class UpdateUserRequest {
        @Schema(description = "New user's name", example = "Jane Doe")
        private String name;
    }
}
