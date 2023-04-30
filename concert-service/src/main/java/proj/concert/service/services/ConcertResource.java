package proj.concert.service.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import proj.concert.common.dto.*;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import proj.concert.service.domain.*;
import proj.concert.service.mapper.*;;

@Path("/concert-service")
public class ConcertResource {

    // private static Logger LOGGER =
    // LoggerFactory.getLogger(ConcertResource.class);


    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾

    @GET
    @Path("/concerts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSingleConcert(@PathParam("id") long id) {
        Concert concert;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            concert = em.find(Concert.class, id);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return concert != null ? Response.ok(ConcertMapper.toConcertDTO(concert)).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/concerts")
    public Response getAllConcerts() {
        Concert[] concerts;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            concerts = em.createQuery("SELECT c FROM Concert c", Concert.class).getResultList()
                    .toArray(new Concert[0]);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        ConcertDTO[] concertDTOs = new ConcertDTO[concerts.length];
        for (int i = 0; i < concerts.length; i++) {
            concertDTOs[i] = ConcertMapper.toConcertDTO(concerts[i]);
        }

        return concertDTOs != null ? Response.ok(concertDTOs).build()
                : Response.status(Response.Status.NO_CONTENT).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/concerts/summaries")
    public Response getConcertSummaries() {
        Concert[] concerts;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            concerts = em.createQuery("SELECT c FROM Concert c", Concert.class).getResultList()
                    .toArray(new Concert[0]);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        ConcertSummaryDTO[] concertSummaryDTOs = new ConcertSummaryDTO[concerts.length];
        for (int i = 0; i < concerts.length; i++) {
            concertSummaryDTOs[i] = ConcertMapper.toConcertSummaryDTO(concerts[i]);
        }

        return concerts != null ? Response.ok(concertSummaryDTOs).build()
                : Response.status(Response.Status.NO_CONTENT).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/performers/{id}")
    public Response getSinglePerformer(@PathParam("id") long id) {
        Performer performer;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            performer = em.find(Performer.class, id);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return performer != null ? Response.ok(PerformerMapper.toPerformerDTO(performer)).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/performers")
    public Response getAllPerformers() {
        Performer[] performers;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            performers = em.createQuery("SELECT p FROM Performer p", Performer.class).getResultList()
                    .toArray(new Performer[0]);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        PerformerDTO[] performerDTOs = new PerformerDTO[performers.length];
        for (int i = 0; i < performers.length; i++) {
            performerDTOs[i] = PerformerMapper.toPerformerDTO(performers[i]);
        }

        return performers != null ? Response.ok(performerDTOs).build()
                : Response.status(Response.Status.NO_CONTENT).build();
    }


    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response authUser(UserDTO userDTO) {
        User user;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            user = em.createQuery("SELECT u FROM User u WHERE u.username = ?1 AND u.password = ?2", User.class)
                    .setParameter(1, userDTO.getUsername())
                    .setParameter(2, userDTO.getPassword())
                    .getSingleResult();
            em.getTransaction().commit();
        } catch (NoResultException e) {
            user = null;
        } finally {
            em.close();
        }

        return user != null ? Response.ok().cookie(getCookie(user.hashCode())).build()
                : Response.status(Status.UNAUTHORIZED).build();
    }

    private NewCookie getCookie(int id) {
        return new NewCookie("auth", Integer.toString(id));
    }


    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾

    @GET
    @Produces(MediaType.APPLICATION_JSON)
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

        EntityManager em = PersistenceManager.instance().createEntityManager();
        List<Seat> seats;
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


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/bookings")
    // TODO BookingRequestDTO compatibility issues; encounters 400 error
    public Response makeBooking(@CookieParam("auth") Cookie auth) {
        if (auth == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        // TODO Make Successful Booking
        return Response.created(null).build();
    }

}
