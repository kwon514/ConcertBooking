package proj.concert.service.mapper;

import java.util.ArrayList;
import java.util.List;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;

public class BookingMapper {
    public static BookingDTO toBookingDTO(Booking booking) {
        List<SeatDTO> seatList = new ArrayList<SeatDTO>();
        for (Seat seat : booking.getSeats()) {
            seatList.add(SeatMapper.toSeatDTO(seat));
        }
        BookingDTO dto = new BookingDTO(booking.getConcertId(), booking.getDate(), seatList);
        return dto;
    }
}