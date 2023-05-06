package proj.concert.service.services;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;
import proj.concert.service.mapper.*;
import static proj.concert.service.services.Services.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
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


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/concert-service")
public class BookResource {

    // generate cookie
    private NewCookie getCookie(Long id) {
        return new NewCookie("auth", Long.toString(id));
    }

    // authorise user
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @POST
    @Path("/login")
    public Response authUser(UserDTO userDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            User user = em
                    .createQuery("SELECT u FROM User u WHERE u.username=?1 AND u.password=?2", User.class)
                    .setParameter(1, userDTO.getUsername())
                    .setParameter(2, userDTO.getPassword())
                    .getSingleResult();
            return Response.ok().cookie(getCookie(user.getId())).build();
        } catch (NoResultException e) {
            return Response.status(Status.UNAUTHORIZED).build();
        } finally {
            em.close();
        }
    }


    // get seats
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/seats/{date}")
    public Response getSeats(
            @PathParam("date") String date,
            @QueryParam("status") String status
    ) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            List<Seat> seats = em
                    .createNamedQuery("Seat.get" + status, Seat.class)
                    .setParameter(1, LocalDateTime.parse(date))
                    .getResultList();
            return Response.ok(generify(BookMapper.toSeatDTOs(seats))).build();
        } finally {
            em.close();
        }
    }


    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    // BOOKINGS
    // _________________________________________________________________________

    // make booking
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @POST
    @Path("/bookings")
    public Response makeBooking(
            BookingRequestDTO bookingRequestDTO,
            @CookieParam("auth") Cookie auth
    ) {
        if (auth == null) return Response.status(Status.UNAUTHORIZED).build();
        // ---------------------------------------------------------------------
        Booking booking;
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // begin transaction
            em.getTransaction().begin();
            // query and verify concert
            Concert concert = em.find(Concert.class, bookingRequestDTO.getConcertId());
            if (concert == null || !concert.getDates().contains(bookingRequestDTO.getDate())) {
                throw new NoResultException("Concert not found");
            }
            // query and verify seats
            List<Seat> seats = em
                    .createQuery("SELECT s FROM Seat s WHERE s.date=?1 AND s.label IN ?2 AND s.isBooked=false", Seat.class)
                    .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                    .setParameter(1, bookingRequestDTO.getDate())
                    .setParameter(2, bookingRequestDTO.getSeatLabels())
                    .getResultList();
            if (seats.size() != bookingRequestDTO.getSeatLabels().size()) {
                throw new OptimisticLockException("Seat(s) already booked");
            }
            // merge seats
            for (Seat seat : seats) {
                seat.setBooked(true);
                em.merge(seat);
            }
            // persist booking
            booking = new Booking(
                    Long.valueOf(auth.getValue()),
                    bookingRequestDTO.getConcertId(),
                    bookingRequestDTO.getDate(),
                    seats
            );
            em.persist(booking);
            // commit transaction
            em.getTransaction().commit();
        } catch (NoResultException e) {
            return Response.status(Status.BAD_REQUEST).entity(e).build();
        } catch (OptimisticLockException e) {
            return Response.status(Status.FORBIDDEN).entity(e).build();
        } finally {
            em.close();
        }

        // when a new booking is made, we need to check if we need to send a notification to the subscribers.
        SubscribeResource.sendNotification(bookingRequestDTO.getConcertId(), bookingRequestDTO.getDate());

        return Response.created(URI.create("/concert-service/bookings/" + booking.getId())).build();
    }

    // get booking
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/bookings/{id}")
    public Response getBooking(
            @PathParam("id") long id,
            @CookieParam("auth") Cookie auth
    ) {
        if (auth == null) return Response.status(Status.UNAUTHORIZED).build();
        // ---------------------------------------------------------------------
        Booking booking = get(Booking.class, id);
        if (booking == null) return Response.status(Status.NOT_FOUND).build();

        return booking.getUserId() == Long.valueOf(auth.getValue()) ?
                Response.ok(BookMapper.toBookingDTO(booking)).build() :
                Response.status(Status.FORBIDDEN).build();
    }

    // get bookings
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/bookings")
    public Response getBookings(@CookieParam("auth") Cookie auth) {
        if (auth == null) return Response.status(Status.UNAUTHORIZED).build();
        // ---------------------------------------------------------------------
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            List<Booking> bookings = em
                    .createQuery("SELECT b FROM Booking b WHERE b.userId=?1", Booking.class)
                    .setParameter(1, Long.valueOf(auth.getValue()))
                    .getResultList();
            return Response.ok(generify(BookMapper.toBookingDTOs(bookings))).build();
        } finally {
            em.close();
        }
    }

}