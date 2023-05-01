package proj.concert.service.services;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;
import proj.concert.service.mapper.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/concert-service")
public class BookResource
{

    // get seats
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/seats/{date}")
    public Response getSeats(
            @PathParam("date") String date,
            @QueryParam("status") String status
    ) {
        // evaluate query
        String qlString = "SELECT s FROM Seat s WHERE s.date=?1";
        switch (status) {
            case "Booked"  : qlString += " AND s.isBooked=true"; break;
            case "Unbooked": qlString += " AND s.isBooked=false";
        }

        List<Seat> seats;

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            seats = em.createQuery(qlString, Seat.class)
                    .setParameter(1, LocalDateTime.parse(date))
                    .getResultList();
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        List<SeatDTO> seatDTOs = new ArrayList<SeatDTO>();
        seats.forEach(e -> seatDTOs.add(SeatMapper.mapSeat(e)));      

        return Response.ok(seatDTOs).build();
    }


    // make booking
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @POST
    @Path("/bookings")
    public Response makeBooking(
        BookingRequestDTO bookingRequestDTO,
        @CookieParam("auth") Cookie auth
    ) {
        // 401 UNAUTHORISED
        if (auth == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        // 201 AUTHORISED
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            for (String seatLabel : bookingRequestDTO.getSeatLabels()) {
                Seat seat = em.createQuery("SELECT s FROM Seat s WHERE s.date=?1 AND s.label=?2", Seat.class)
                        .setParameter(1, bookingRequestDTO.getDate())
                        .setParameter(2, seatLabel)
                        .getSingleResult();
                seat.setBooked(true);
                em.merge(seat);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        // dummy URI
        return Response.created(URI.create("")).build();
    }

}