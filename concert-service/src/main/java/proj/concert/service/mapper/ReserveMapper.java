package proj.concert.service.mapper;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class ReserveMapper {

    // booking to booking dto
    public static BookingDTO toBookingDTO(Booking booking) {
        return new BookingDTO(
                booking.getConcertId(),
                booking.getDate(),
                toSeatDTOs(booking.getSeats())
        );
    }

    // seat to seat dto
    public static SeatDTO toSeatDTO(Seat seat) {
        return new SeatDTO(seat.getLabel(), seat.getCost());
    }

    // bookings to list of booking dtos
    public static List<BookingDTO> toBookingDTOs(Collection<Booking> bookings) {
        return bookings.stream().map(e -> toBookingDTO(e)).collect(Collectors.toList());
    }

    // seats to list of seat dtos
    public static List<SeatDTO> toSeatDTOs(Collection<Seat> seats) {
        return seats.stream().map(e -> toSeatDTO(e)).collect(Collectors.toList());
    }

}