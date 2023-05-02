package proj.concert.service.mapper;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;

import java.util.List;
import java.util.stream.Collectors;

public class SeatMapper {
    public static SeatDTO mapSeat(Seat seat) {
        return new SeatDTO(seat.getLabel(), seat.getCost());
    }

    public static List<SeatDTO> mapSeats(List<Seat> seats) {
        return seats.stream().map(e -> mapSeat(e)).collect(Collectors.toList());
    }

    public static SeatDTO toSeatDTO(Seat seat) {
        SeatDTO dto = new SeatDTO(seat.getLabel(), seat.getCost());
        return dto;
    }
}
