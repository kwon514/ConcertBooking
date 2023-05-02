package proj.concert.service.services;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;
import proj.concert.service.mapper.*;

import java.net.URI;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/concert-service")
public class BookResource {

    // Get seats by date
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/seats/{date}")
    public Response getSeats(
            @PathParam("date") String date,
            @QueryParam("status") String status) {
        List<Seat> seats;

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            seats = em.createNamedQuery("Seat.get" + status, Seat.class)
                    .setParameter(1, LocalDateTime.parse(date))
                    .getResultList();
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        List<SeatDTO> seatDTOs = SeatMapper.mapSeats(seats);

        return Response.ok(seatDTOs).build();
    }

    // Make a booking
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @POST
    @Path("/bookings")
    public Response makeBooking(
            BookingRequestDTO bookingRequestDTO,
            @CookieParam("auth") Cookie auth) {
        if (auth == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        Booking booking;
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            // Check if the concert exists
            Concert concert = em.find(Concert.class, bookingRequestDTO.getConcertId());
            if (concert == null) {
                return Response.status(Status.BAD_REQUEST).entity("Concert not found").build();
            }

            // Book the seats
            List<Seat> seats = new ArrayList<>();
            for (String seatLabel : bookingRequestDTO.getSeatLabels()) {
                Seat seat = em.createQuery("SELECT s FROM Seat s WHERE s.date=?1 AND s.label=?2", Seat.class)
                        .setParameter(1, bookingRequestDTO.getDate())
                        .setParameter(2, seatLabel)
                        .getSingleResult();
                if (seat.isBooked()) {
                    return Response.status(Status.FORBIDDEN).entity("Seat already booked").build();
                }
                seat.setBooked(true);
                em.merge(seat);
                seats.add(seat);
            }

            booking = new Booking(Long.valueOf(auth.getValue()), bookingRequestDTO.getConcertId(),
                    bookingRequestDTO.getDate(), seats);
            em.persist(booking);
            em.getTransaction().commit();
        } catch (NoResultException e) {
            return Response.status(Status.BAD_REQUEST).build();
        } finally {
            em.close();
        }

        return Response.created(URI.create("/concert-service/bookings/" + booking.getId())).build();
    }

    // Get all bookings for a user
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/bookings")
    public Response getBooking(
            @CookieParam("auth") Cookie auth) {
        if (auth == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        List<Booking> bookings;
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            bookings = em.createQuery("SELECT b FROM Booking b WHERE b.userId=?1", Booking.class)
                    .setParameter(1, Long.valueOf(auth.getValue()))
                    .getResultList();
            em.getTransaction().commit();
        } catch (NoResultException e) {
            return Response.status(Status.NOT_FOUND).build();
        } finally {
            em.close();
        }

        List<BookingDTO> bookingDTOs = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDTOs.add(BookingMapper.toBookingDTO(booking));
        }
        return bookings != null ? Response.ok(bookingDTOs).build() : Response.status(Status.NOT_FOUND).build();
    }

    // Get a booking for the user by id
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/bookings/{id}")
    public Response getBookingById(
            @PathParam("id") long id,
            @CookieParam("auth") Cookie auth) {
        if (auth == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        Booking booking;
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            booking = em.createQuery("SELECT b FROM Booking b WHERE b.id=?1", Booking.class)
                    .setParameter(1, id)
                    .getSingleResult();
            em.getTransaction().commit();
        } catch (NoResultException e) {
            return Response.status(Status.NOT_FOUND).build();
        } finally {
            em.close();
        }
        if (booking.getUserId() != Long.valueOf(auth.getValue())) {
            return Response.status(Status.FORBIDDEN).build();
        }
        return booking != null ? Response.ok(BookingMapper.toBookingDTO(booking)).build()
                : Response.status(Status.NOT_FOUND).build();
    }
}