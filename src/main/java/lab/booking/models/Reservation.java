package lab.booking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", schema = "main")
@Data
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "guest", nullable = false)
    private User guest;

    @ManyToOne
    @JoinColumn(name = "room", nullable = false)
    private Room room;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Reservation(User guest, Room room, LocalDate fromDate, LocalDate toDate) {
        this.guest = guest;
        this.room = room;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.createdAt = LocalDateTime.now();
    }
}
