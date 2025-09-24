package lab.booking.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.booking.controllers.UserController.CreateUserRequest;
import lab.booking.controllers.UserController.UpdateUserRequest;
import lab.booking.exceptions.UserNotFoundException;
import lab.booking.models.User;
import lab.booking.services.BookingService;
import lab.booking.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

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

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/users";

        testUser = new User();
        testUser.setId(1);
        testUser.setName("John Doe");
        testUser.setRole(UserRole.USER);
    }

    @Test
    void getAllUsers_ShouldReturnUsersList() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(bookingService.getAllUsers()).thenReturn(users);

        // Act
        ResponseEntity<User[]> response = restTemplate.getForEntity(baseUrl, User[].class);

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
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

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
        ResponseEntity<User> response = restTemplate.getForEntity(baseUrl + "/1", User.class);

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
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/999", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User with id 999 not found");

        verify(bookingService, times(1)).getUserById(999);
    }

    @Test
    void createUser_WithValidData_ShouldReturnCreatedUser() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John Doe");
        request.setRole(UserRole.USER);

        when(bookingService.createUser("John Doe", UserRole.USER)).thenReturn(testUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<User> response = restTemplate.postForEntity(baseUrl, entity, User.class);

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

        CreateUserRequest request = new CreateUserRequest();
        request.setName("");
        request.setRole(UserRole.USER);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid user data");
    }

    @Test
    void updateUser_WithValidData_ShouldReturnSuccessMessage() {
        // Arrange
        doNothing().when(bookingService).updateUserName(1, "Jane Doe");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Jane Doe");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/1", HttpMethod.PUT, entity, String.class);

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

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Jane Doe");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999", HttpMethod.PUT, entity, String.class);

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
                baseUrl + "/1", HttpMethod.DELETE, null, String.class);

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
                baseUrl + "/999", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User with id 999 not found");

        verify(bookingService, times(1)).deleteUser(999);
    }
}
