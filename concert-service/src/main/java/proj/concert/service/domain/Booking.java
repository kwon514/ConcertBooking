package proj.concert.service.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long id;

    @Column(name = "USER_ID")
    private long userId;

    @Column(name = "CONCERT_ID")
    private long concertId;

    @Column(name = "DATE")
    private LocalDateTime date;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "SEAT")
    private List<Seat> seats = new ArrayList<>();

    public Booking() {
    }

    public Booking(long userId, long concertId, LocalDateTime date, List<Seat> seats) {
        this.userId = userId;
        this.concertId = concertId;
        this.date = date;
        this.seats = seats;
    }

    public long getId() {
        return id;
    }

    public void setId(long bookingId) {
        this.id = bookingId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}
