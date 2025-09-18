package lab.booking.exceptions;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Integer number) {
        super("Room with number " + number + " not found");
    }

    public ReservationNotFoundException(String message) {
        super(message);
    }
}
