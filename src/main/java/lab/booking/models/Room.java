package lab.booking.models;

import lab.booking.enums.RoomType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms", schema = "main")
@Data
@NoArgsConstructor
public class Room {

    @Id
    @Column(name = "number")
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    public Room(Integer number, RoomType type) {
        this.number = number;
        this.type = type;
    }
}
