package lab.booking.services;

import lab.booking.enums.*;
import lab.booking.models.*;
import lab.booking.repositories.*;
import lab.booking.exceptions.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    // ================= USERS =================

    public User createUser(String name, UserRole role) {
        return userRepository.save(new User(name, role));
    }

    public void deleteUser(Integer id) {
        userRepository.delete(getUserById(id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void updateUserName(Integer id, String name) {
        int updated = userRepository.updateUserName(id, name);
        if (updated == 0) throw new UserNotFoundException(id);
    }

    // ================= ROOMS =================

    public Room createRoom(Integer number, RoomType type) {
        return roomRepository.save(new Room(number, type));
    }

    public void deleteRoom(Integer number) {
        roomRepository.delete(getRoomByNumber(number));
    }

    public Room getRoomByNumber(Integer number) {
        return roomRepository.findByNumber(number)
                .orElseThrow(() -> new RoomNotFoundException(number));
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getAvailableRooms(LocalDateTime fromDate, LocalDateTime toDate) {
        return roomRepository.findAvailableRooms(fromDate, toDate);
    }

    // ================= RESERVATIONS =================

    @Transactional
    public Reservation createReservation(Integer guestId, Integer roomNumber,
                                         LocalDateTime fromDate, LocalDateTime toDate) {
        User guest = getUserById(guestId);
        Room room = roomRepository.findById(roomNumber)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return reservationRepository.save(new Reservation(guest, room, fromDate, toDate));
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getUserReservations(Integer guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    public void cancelReservation(Integer reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
