package lab.booking.repositories;

import lab.booking.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    @Query("""
        SELECT r FROM Room r WHERE r.number NOT IN (
            SELECT res.room.number FROM Reservation res WHERE 
            res.fromDate < :toDate AND res.toDate > :fromDate
        )
        """)
    List<Room> findAvailableRooms(@Param("fromDate") LocalDateTime fromDate,
                                  @Param("toDate") LocalDateTime toDate);

    Optional<Room> findByNumber(Integer number);
}
