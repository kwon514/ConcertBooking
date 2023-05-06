package proj.concert.service.services;

import proj.concert.common.dto.*;
import proj.concert.service.domain.*;
import proj.concert.service.util.TheatreLayout;
import static proj.concert.service.services.Services.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/concert-service")
public class SubscribeResource {

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
    public void subscribeToConcertInfo(
        ConcertInfoSubscriptionDTO subscriptionRequestDTO,
        @Suspended AsyncResponse asyncResponse,
        @CookieParam("auth") Cookie auth
    ) {    
            // first we want to check if the user is authenticated
            if (auth == null) {
                asyncResponse.resume(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }
            
            // if user is authenticated, we proceed with checking if the concert and date is valid.    

            // we need to check whether the requested concert and date exist.
            Concert concert = get(Concert.class, subscriptionRequestDTO.getConcertId());
            if (concert == null || !concert.getDates().contains(subscriptionRequestDTO.getDate())) {
                asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
                return;
            }

            // if this concert has no subscibers yet, need to initialise a new list for subscribers.
            if (!subscriptions.containsKey(subscriptionRequestDTO.getConcertId())) {
                subscriptions.put(subscriptionRequestDTO.getConcertId(), new ArrayList<>());
            } 
            
            // if the checks pass, we can update and store the subscription in the subscription list of the that concert.
            subscriptions.get(subscriptionRequestDTO.getConcertId()).add(new Subscription(subscriptionRequestDTO, asyncResponse));  
    }

    /**
     * the function takes a concertID and and date which is uses to get the bookedSeats from here,
     * it can check all the subscriptions for that particular concertID and can send a notification to the subscribers.
     * this function needs to be called after a booking is made so that a notification can be sent.
     * @param concertId
     * @param time
     */
    public static void sendNotification(Long concertId, LocalDateTime time) {
        threadPool.submit(() -> {
            // we don't need to send a notification if the concert has no subscribers.
            if (!subscriptions.containsKey(concertId)) {
                return;
            }

            // we must query the seats to find all the seats that are booked for the given concert time.
            List<Seat> seats = getSeats(time, "Booked");            
            // we query to find the actual amount of seats booked in the given concert.
            final int numOfSeatsBooked = seats.size();
            // total number of seats in theatre
            final var totalNumOfSeats = TheatreLayout.NUM_SEATS_IN_THEATRE;
            // we calculate the percentage of seats that have been booked by using the NUM_SEATS_IN_THEATRE.
            final var percentageOfBookedSeats = (numOfSeatsBooked / (double)totalNumOfSeats) * 100.0;

            // otherwise, we will need to go through all the subscriptions of that concert 
            // and send them a notification if the percentage of actual seats booked exceeds their desired percentage.
            for (Subscription subscription : subscriptions.get(concertId)) {
                if (percentageOfBookedSeats > (float)subscription.getConcertInfoSubscriptionDTO().getPercentageBooked()) {
                    // we then need to resume the AsyncResponses to send out the notification of how many seats are remaining.
                    subscription.getAsyncResponse().resume(
                                Response
                                    .status(Response.Status.OK)
                                    .entity(new ConcertInfoNotificationDTO(totalNumOfSeats - numOfSeatsBooked))
                                    .build());
                } else {
                    continue; // if the subscription condition is not met, don't send notification.
                }
            }
        });
    }

}
