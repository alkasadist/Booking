package lab.booking.exceptions;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Integer id) {
        super("Reservation with id " + id + " not found");
    }

    public ReservationNotFoundException(String message) {
        super(message);
    }
}
