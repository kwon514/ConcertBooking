package proj.concert.service.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;

public class ConcertMapper {
    public static ConcertDTO toConcertDTO(Concert concert) {
        ConcertDTO dto = new ConcertDTO(concert.getId(), concert.getTitle(), concert.getImageName(),
                concert.getBlurb());
        List<PerformerDTO> performerList = new ArrayList<PerformerDTO>();
        List<LocalDateTime> dateList = new ArrayList<LocalDateTime>();

        for (Performer performer : concert.getPerformers()) {
            performerList.add(PerformerMapper.toPerformerDTO(performer));
        }
        dto.setPerformers(performerList);

        dateList.addAll(concert.getDates());
        dto.setDates(dateList);
        return dto;
    }

    public static ConcertSummaryDTO toConcertSummaryDTO(Concert concert) {
        return new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName());
    }
}
