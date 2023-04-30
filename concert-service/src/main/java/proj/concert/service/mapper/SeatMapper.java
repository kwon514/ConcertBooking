package proj.concert.service.mapper;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;

public class SeatMapper {
    public static SeatDTO mapSeat(Seat seat) {
        return new SeatDTO(seat.getLabel(), seat.getCost());
    }
}
