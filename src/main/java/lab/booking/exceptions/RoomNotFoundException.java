package lab.booking.exceptions;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(Integer number) {
        super("Room with number " + number + " not found");
    }

    public RoomNotFoundException(String message) {
        super(message);
    }
}
