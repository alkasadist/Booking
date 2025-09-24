package lab.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.booking.controllers.RoomController.CreateRoomRequest;
import lab.booking.exceptions.RoomNotFoundException;
import lab.booking.models.Room;
import lab.booking.services.BookingService;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RoomControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/rooms";

        testRoom = new Room();
        testRoom.setNumber(101);
        testRoom.setType(RoomType.ECONOMY);
    }

    @Test
    void getAllRooms_ShouldReturnRoomsList() {
        // Arrange
        List<Room> rooms = Arrays.asList(testRoom);
        when(bookingService.getAllRooms()).thenReturn(rooms);

        // Act
        ResponseEntity<Room[]> response = restTemplate.getForEntity(baseUrl, Room[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getNumber()).isEqualTo(101);
        assertThat(response.getBody()[0].getType()).isEqualTo(RoomType.ECONOMY);

        verify(bookingService, times(1)).getAllRooms();
    }

    @Test
    void getAllRooms_WhenServiceThrowsException_ShouldReturn500() {
        // Arrange
        when(bookingService.getAllRooms()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Internal server error");

        verify(bookingService, times(1)).getAllRooms();
    }

    @Test
    void getRoomByNumber_WithExistingNumber_ShouldReturnRoom() {
        // Arrange
        when(bookingService.getRoomByNumber(101)).thenReturn(testRoom);

        // Act
        ResponseEntity<Room> response = restTemplate.getForEntity(baseUrl + "/101", Room.class);

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
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/999", String.class);

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
        String url = baseUrl + "/available?fromDate=2025-10-01&toDate=2025-10-05";
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
        String url = baseUrl + "/available?fromDate=invalid-date&toDate=2025-10-05";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid date format");
    }

    @Test
    void getAvailableRooms_WithFromDateAfterToDate_ShouldReturn400() {
        // Act
        String url = baseUrl + "/available?fromDate=2025-10-10&toDate=2025-10-05";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Check-in date must be before check-out date");
    }

    @Test
    void createRoom_WithValidData_ShouldReturnCreatedRoom() {
        // Arrange
        CreateRoomRequest request = new CreateRoomRequest();
        request.setNumber(101);
        request.setType(RoomType.ECONOMY);

        when(bookingService.createRoom(101, RoomType.ECONOMY)).thenReturn(testRoom);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateRoomRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Room> response = restTemplate.postForEntity(baseUrl, entity, Room.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo(101);
        assertThat(response.getBody().getType()).isEqualTo(RoomType.ECONOMY);

        verify(bookingService, times(1)).createRoom(101, RoomType.ECONOMY);
    }

    @Test
    void createRoom_WithInvalidData_ShouldReturn400() {
        // Arrange
        when(bookingService.createRoom(anyInt(), any(RoomType.class)))
                .thenThrow(new IllegalArgumentException("Room number must be positive"));

        CreateRoomRequest request = new CreateRoomRequest();
        request.setNumber(-1);
        request.setType(RoomType.ECONOMY);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateRoomRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid room data");
    }

    @Test
    void deleteRoom_WithExistingNumber_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(bookingService).deleteRoom(101);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/101", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Room deleted successfully");

        verify(bookingService, times(1)).deleteRoom(101);
    }

    @Test
    void deleteRoom_WithNonExistingNumber_ShouldReturn404() {
        // Arrange
        doThrow(new RoomNotFoundException("Room not found"))
                .when(bookingService).deleteRoom(999);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Room with number 999 not found");

        verify(bookingService, times(1)).deleteRoom(999);
    }
}
