package proj.concert.service.services;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;
import proj.concert.service.util.TheatreLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.persistence.EntityManager;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/concert-service")
public class SubscribeResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeResource.class);

    // set up a class to represent the subscriptions.
    // a subscription is made up of a ConcertInfoSubscriptionDTO and an AsyncResponse.
    static class Subscription {
        private final ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO;
        private final AsyncResponse asyncResponse;

        public Subscription(ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO, AsyncResponse asyncResponse) {
            this.concertInfoSubscriptionDTO = concertInfoSubscriptionDTO;
            this.asyncResponse = asyncResponse;
        }

        public ConcertInfoSubscriptionDTO getConcertInfoSubscriptionDTO() {
            return concertInfoSubscriptionDTO;
        }

        public AsyncResponse getAsyncResponse() {
            return asyncResponse;
        }
    }

    // initialise the map where we will keep track of the subscriptions.
    // the map keeps track of concerts by their id as one concert may have many subscriptions.
    private static final Map<Long, List<Subscription>> subscriptions = new HashMap<>();
    
    // initialise the thread pool (like in lecture 10 example).
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    // Process subscription request
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @POST
    @Path("subscribe/concertInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public void subscribeToConcertInfo(
        ConcertInfoSubscriptionDTO subscriptionRequestDTO,
        @Suspended AsyncResponse asyncResponse,
        @CookieParam("auth") Cookie auth) {
    
            // first we want to check if the user is authenticated
            if (auth == null) {
                asyncResponse.resume(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }
            
            // if user is authenticated, we proceed with checking if the concert and date is valid.    

            // we need to check whether the requested concert and date exist.
            EntityManager em = PersistenceManager.instance().createEntityManager();
            try {
                em.getTransaction().begin();

                // check if the concert exists.
                Concert concert = em.find(Concert.class, subscriptionRequestDTO.getConcertId());
                if (concert == null) {
                    throw new Exception();
                }

                // check if the date is valid (that is, if concert date in request is in fact a date in that concert schedule)
                Set<LocalDateTime> dateList = new HashSet<>();
                dateList = concert.getDates();
                if (!dateList.contains(subscriptionRequestDTO.getDate())) {
                    throw new Exception();
                }

                // if this concert has no subscibers yet, need to initialise a new list for subscribers.
                if (!subscriptions.containsKey(subscriptionRequestDTO.getConcertId())) {
                    subscriptions.put(subscriptionRequestDTO.getConcertId(), new ArrayList<>());
                } 
                
                // if the checks pass, we can update and store the subscription in the subscription list of the that concert.
                subscriptions.get(subscriptionRequestDTO.getConcertId()).add(new Subscription(subscriptionRequestDTO, asyncResponse));  
            
            } catch (Exception e) {
                asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());   // we handle the bad requests here.
                return;
            } finally {
                em.close(); // last thing to do is to close the persistence manager.
            }
    }

    // the function takes a concertID and and date which is uses to get the bookedSeats
    // from here, it can check all the subscriptions for that particular concertID 
    // and can send a notification to the subscribers. 
    // this function needs to be called after a booking is made so that a notification can be sent.
    public void sendNotification(Long concertId, LocalDateTime time) {
        threadPool.submit(() -> {
            EntityManager em = PersistenceManager.instance().createEntityManager();
            
            // we must query the seats to find all the seats that are booked for the given concert time.
            List<Seat> seats = new ArrayList<>();
            seats = em.createQuery("SELECT s FROM Seat s WHERE s.date = ?1 AND s.isBooked = true", Seat.class)
                    .setParameter(1, time)
                    .getResultList();
            
            // we query to find the actual amount of seats booked in the given concert.
            final int numOfSeatsBooked = seats.size();
            
            // total number of seats in theatre
            final var totalNumOfSeats = TheatreLayout.NUM_SEATS_IN_THEATRE;

            // we calculate the percentage of seats that have been booked by using the NUM_SEATS_IN_THEATRE.
            final var percentageOfBookedSeats = (numOfSeatsBooked / (double)totalNumOfSeats) * 100.0;

            em.close();
            
            // we don't need to send a notification if the concert has no subscribers.
            if (!subscriptions.containsKey(concertId)) {
                return;
            }

            // otherwise, we will need to go through all the subscriptions of that concert 
            // and send them a notification if the percentage of actual seats booked exceeds their desired percentage.
            for (Subscription subscription : subscriptions.get(concertId)) {
                if (percentageOfBookedSeats > (float)subscription.getConcertInfoSubscriptionDTO().getPercentageBooked()) {
                    // we then need to resume the AsyncResponses to send out the notifiation of how many seats are remaining.
                    subscription.getAsyncResponse().resume(
                                Response.status(Response.Status.OK)
                                .entity(new ConcertInfoNotificationDTO(totalNumOfSeats - numOfSeatsBooked))
                                .build());
                }
                else {
                    continue;  // if the subscription condition is not met, don't send notification.
                }
            }
        });
    }
}
