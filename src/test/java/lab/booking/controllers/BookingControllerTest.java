package lab.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.booking.exceptions.UserNotFoundException;
import lab.booking.exceptions.RoomNotFoundException;
import lab.booking.models.User;
import lab.booking.models.Room;
import lab.booking.models.Reservation;
import lab.booking.services.BookingService;
import lab.booking.enums.UserRole;
import lab.booking.enums.RoomType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookingControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private User testUser;
    private Room testRoom;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";

        testUser = new User();
        testUser.setId(1);
        testUser.setName("John Doe");
        testUser.setRole(UserRole.USER);

        testRoom = new Room();
        testRoom.setNumber(101);
        testRoom.setType(RoomType.ECONOMY);

        testReservation = new Reservation();
        testReservation.setId(1);
        testReservation.setGuest(testUser);
        testReservation.setRoom(testRoom);
        testReservation.setFromDate(LocalDate.of(2025, 10, 1));
        testReservation.setToDate(LocalDate.of(2025, 10, 5));
        testReservation.setCreatedAt(LocalDateTime.now());
    }

    // ================= USER TESTS =================

    @Test
    void getAllUsers_ShouldReturnUsersList() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(bookingService.getAllUsers()).thenReturn(users);

        // Act
        ResponseEntity<User[]> response = restTemplate.getForEntity(
                baseUrl + "/users", User[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getId()).isEqualTo(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("John Doe");
        assertThat(response.getBody()[0].getRole()).isEqualTo(UserRole.USER);

        verify(bookingService, times(1)).getAllUsers();
    }

    @Test
    void getAllUsers_WhenServiceThrowsException_ShouldReturn500() {
        // Arrange
        when(bookingService.getAllUsers()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/users", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Internal server error");

        verify(bookingService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        // Arrange
        when(bookingService.getUserById(1)).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = restTemplate.getForEntity(
                baseUrl + "/users/1", User.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1);
        assertThat(response.getBody().getName()).isEqualTo("John Doe");

        verify(bookingService, times(1)).getUserById(1);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldReturn404() {
        // Arrange
        when(bookingService.getUserById(999)).thenThrow(new UserNotFoundException("User not found"));

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/users/999", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User with id 999 not found");

        verify(bookingService, times(1)).getUserById(999);
    }

    @Test
    void createUser_WithValidData_ShouldReturnCreatedUser() {
        // Arrange
        BookingController.CreateUserRequest request = new BookingController.CreateUserRequest();
        request.setName("John Doe");
        request.setRole(UserRole.USER);

        when(bookingService.createUser("John Doe", UserRole.USER)).thenReturn(testUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.CreateUserRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<User> response = restTemplate.postForEntity(
                baseUrl + "/users", entity, User.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("John Doe");
        assertThat(response.getBody().getRole()).isEqualTo(UserRole.USER);

        verify(bookingService, times(1)).createUser("John Doe", UserRole.USER);
    }

    @Test
    void createUser_WithInvalidData_ShouldReturn400() {
        // Arrange
        when(bookingService.createUser(anyString(), any(UserRole.class)))
                .thenThrow(new IllegalArgumentException("Name cannot be empty"));

        BookingController.CreateUserRequest request = new BookingController.CreateUserRequest();
        request.setName("");
        request.setRole(UserRole.USER);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.CreateUserRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/users", entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid user data");
    }

    @Test
    void updateUser_WithValidData_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(bookingService).updateUserName(1, "Jane Doe");

        BookingController.UpdateUserRequest request = new BookingController.UpdateUserRequest();
        request.setName("Jane Doe");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.UpdateUserRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users/1", HttpMethod.PUT, entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("User updated successfully");

        verify(bookingService, times(1)).updateUserName(1, "Jane Doe");
    }

    @Test
    void updateUser_WithNonExistingId_ShouldReturn404() {
        // Arrange
        doThrow(new UserNotFoundException("User not found"))
                .when(bookingService).updateUserName(999, "Jane Doe");

        BookingController.UpdateUserRequest request = new BookingController.UpdateUserRequest();
        request.setName("Jane Doe");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.UpdateUserRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users/999", HttpMethod.PUT, entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User with id 999 not found");

        verify(bookingService, times(1)).updateUserName(999, "Jane Doe");
    }

    @Test
    void deleteUser_WithExistingId_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(bookingService).deleteUser(1);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users/1", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("User deleted successfully");

        verify(bookingService, times(1)).deleteUser(1);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldReturn404() {
        // Arrange
        doThrow(new UserNotFoundException("User not found"))
                .when(bookingService).deleteUser(999);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users/999", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User with id 999 not found");

        verify(bookingService, times(1)).deleteUser(999);
    }

    // ================= ROOM TESTS =================

    @Test
    void getAllRooms_ShouldReturnRoomsList() {
        // Arrange
        List<Room> rooms = Arrays.asList(testRoom);
        when(bookingService.getAllRooms()).thenReturn(rooms);

        // Act
        ResponseEntity<Room[]> response = restTemplate.getForEntity(
                baseUrl + "/rooms", Room[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getNumber()).isEqualTo(101);
        assertThat(response.getBody()[0].getType()).isEqualTo(RoomType.ECONOMY);

        verify(bookingService, times(1)).getAllRooms();
    }

    @Test
    void getRoomByNumber_WithExistingNumber_ShouldReturnRoom() {
        // Arrange
        when(bookingService.getRoomByNumber(101)).thenReturn(testRoom);

        // Act
        ResponseEntity<Room> response = restTemplate.getForEntity(
                baseUrl + "/rooms/101", Room.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo(101);
        assertThat(response.getBody().getType()).isEqualTo(RoomType.ECONOMY);

        verify(bookingService, times(1)).getRoomByNumber(101);
    }

    @Test
    void getRoomByNumber_WithNonExistingNumber_ShouldReturn404() {
        // Arrange
        when(bookingService.getRoomByNumber(999))
                .thenThrow(new RoomNotFoundException("Room not found"));

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/rooms/999", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Room with number 999 not found");

        verify(bookingService, times(1)).getRoomByNumber(999);
    }

    @Test
    void getAvailableRooms_WithValidDates_ShouldReturnRoomsList() {
        // Arrange
        LocalDate fromDate = LocalDate.of(2025, 10, 1);
        LocalDate toDate = LocalDate.of(2025, 10, 5);
        List<Room> rooms = Arrays.asList(testRoom);

        when(bookingService.getAvailableRooms(fromDate, toDate)).thenReturn(rooms);

        // Act
        String url = baseUrl + "/rooms/available?fromDate=2025-10-01&toDate=2025-10-05";
        ResponseEntity<Room[]> response = restTemplate.getForEntity(url, Room[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getNumber()).isEqualTo(101);

        verify(bookingService, times(1)).getAvailableRooms(fromDate, toDate);
    }

    @Test
    void getAvailableRooms_WithInvalidDateFormat_ShouldReturn400() {
        // Act
        String url = baseUrl + "/rooms/available?fromDate=invalid-date&toDate=2025-10-05";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid date format");
    }

    @Test
    void getAvailableRooms_WithFromDateAfterToDate_ShouldReturn400() {
        // Act
        String url = baseUrl + "/rooms/available?fromDate=2025-10-10&toDate=2025-10-05";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Check-in date must be before check-out date");
    }

    @Test
    void createRoom_WithValidData_ShouldReturnCreatedRoom() {
        // Arrange
        BookingController.CreateRoomRequest request = new BookingController.CreateRoomRequest();
        request.setNumber(101);
        request.setType(RoomType.ECONOMY);

        when(bookingService.createRoom(101, RoomType.ECONOMY)).thenReturn(testRoom);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.CreateRoomRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Room> response = restTemplate.postForEntity(
                baseUrl + "/rooms", entity, Room.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo(101);
        assertThat(response.getBody().getType()).isEqualTo(RoomType.ECONOMY);

        verify(bookingService, times(1)).createRoom(101, RoomType.ECONOMY);
    }

    @Test
    void deleteRoom_WithExistingNumber_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(bookingService).deleteRoom(101);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/rooms/101", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Room deleted successfully");

        verify(bookingService, times(1)).deleteRoom(101);
    }

    // ================= RESERVATION TESTS =================

    @Test
    void getAllReservations_ShouldReturnReservationsList() {
        // Arrange
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(bookingService.getAllReservations()).thenReturn(reservations);

        // Act
        ResponseEntity<Reservation[]> response = restTemplate.getForEntity(
                baseUrl + "/reservations", Reservation[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getId()).isEqualTo(1);
        assertThat(response.getBody()[0].getGuest().getId()).isEqualTo(1);
        assertThat(response.getBody()[0].getRoom().getNumber()).isEqualTo(101);

        verify(bookingService, times(1)).getAllReservations();
    }

    @Test
    void getUserReservations_WithExistingUserId_ShouldReturnReservationsList() {
        // Arrange
        List<Reservation> reservations = Arrays.asList(testReservation);
        when(bookingService.getUserReservations(1)).thenReturn(reservations);

        // Act
        ResponseEntity<Reservation[]> response = restTemplate.getForEntity(
                baseUrl + "/users/1/reservations", Reservation[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getGuest().getId()).isEqualTo(1);

        verify(bookingService, times(1)).getUserReservations(1);
    }

    @Test
    void getUserReservations_WithNonExistingUserId_ShouldReturn404() {
        // Arrange
        when(bookingService.getUserReservations(999))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/users/999/reservations", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User with id 999 not found");

        verify(bookingService, times(1)).getUserReservations(999);
    }

    @Test
    void createReservation_WithValidData_ShouldReturnCreatedReservation() {
        // Arrange
        BookingController.CreateReservationRequest request = new BookingController.CreateReservationRequest();
        request.setGuestId(1);
        request.setRoomNumber(101);
        request.setFromDate("2025-10-01");
        request.setToDate("2025-10-05");

        when(bookingService.createReservation(1, 101,
                LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 5)))
                .thenReturn(testReservation);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.CreateReservationRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Reservation> response = restTemplate.postForEntity(
                baseUrl + "/reservations", entity, Reservation.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getGuest().getId()).isEqualTo(1);
        assertThat(response.getBody().getRoom().getNumber()).isEqualTo(101);

        verify(bookingService, times(1)).createReservation(1, 101,
                LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 5));
    }

    @Test
    void createReservation_WithNonExistingUser_ShouldReturn404() {
        // Arrange
        BookingController.CreateReservationRequest request = new BookingController.CreateReservationRequest();
        request.setGuestId(999);
        request.setRoomNumber(101);
        request.setFromDate("2025-10-01");
        request.setToDate("2025-10-05");

        when(bookingService.createReservation(eq(999), eq(101), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.CreateReservationRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/reservations", entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User with id 999 not found");
    }

    @Test
    void createReservation_WithInvalidDateFormat_ShouldReturn400() {
        // Arrange
        BookingController.CreateReservationRequest request = new BookingController.CreateReservationRequest();
        request.setGuestId(1);
        request.setRoomNumber(101);
        request.setFromDate("invalid-date");
        request.setToDate("2025-10-05");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.CreateReservationRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/reservations", entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid date format");
    }

    @Test
    void createReservation_WithFromDateAfterToDate_ShouldReturn400() {
        // Arrange
        BookingController.CreateReservationRequest request = new BookingController.CreateReservationRequest();
        request.setGuestId(1);
        request.setRoomNumber(101);
        request.setFromDate("2025-10-10");
        request.setToDate("2025-10-05");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookingController.CreateReservationRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/reservations", entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Check-in date must be before check-out date");
    }

    @Test
    void cancelReservation_WithExistingId_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(bookingService).cancelReservation(1);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/reservations/1", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Reservation cancelled successfully");

        verify(bookingService, times(1)).cancelReservation(1);
    }

    @Test
    void cancelReservation_WithNonExistingId_ShouldReturn404() {
        // Arrange
        doThrow(new RuntimeException("Reservation not found"))
                .when(bookingService).cancelReservation(999);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/reservations/999", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Reservation with id 999 not found");

        verify(bookingService, times(1)).cancelReservation(999);
    }
}
