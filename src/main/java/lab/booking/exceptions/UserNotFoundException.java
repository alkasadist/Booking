package lab.booking.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Integer userId) {
        super("User with ID " + userId + " not found");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
