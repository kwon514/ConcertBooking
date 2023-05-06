package proj.concert.service.mapper;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class ConcertMapper {

    // concert to concert dto
    public static ConcertDTO toConcertDTO(Concert concert) {
        ConcertDTO dto = new ConcertDTO(
                concert.getId(),
                concert.getTitle(),
                concert.getImageName(),
                concert.getBlurb()
        );
        dto.setPerformers(toPerformerDTOs(concert.getPerformers()));
        dto.setDates(new ArrayList<LocalDateTime>(concert.getDates()));
        return dto;
    }

    // concert to concert summary dto
    public static ConcertSummaryDTO toConcertSummaryDTO(Concert concert) {
        return new ConcertSummaryDTO(
                concert.getId(),
                concert.getTitle(),
                concert.getImageName()
        );
    }

    // performer to performer dto
    public static PerformerDTO toPerformerDTO(Performer performer) {
        return new PerformerDTO(
                performer.getId(),
                performer.getName(),
                performer.getImageName(),
                performer.getGenre(),
                performer.getBlurb()
        );
    }

    // concerts to list of concert dtos
    public static List<ConcertDTO> toConcertDTOs(Collection<Concert> concerts) {
        return concerts.stream().map(e -> toConcertDTO(e)).collect(Collectors.toList());
    }

    // concerts to list of concert summary dtos
    public static List<ConcertSummaryDTO> toConcertSummaryDTOs(Collection<Concert> concerts) {
        return concerts.stream().map(e -> toConcertSummaryDTO(e)).collect(Collectors.toList());
    }

    // performers to list of performer dtos
    public static List<PerformerDTO> toPerformerDTOs(Collection<Performer> performers) {
        return performers.stream().map(e -> toPerformerDTO(e)).collect(Collectors.toList());
    }

}
