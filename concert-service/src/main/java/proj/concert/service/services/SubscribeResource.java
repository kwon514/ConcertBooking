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

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.util.Set;
import java.util.Vector;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/concert-service")
public class SubscribeResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeResource.class);
    
    // private final List<AsyncResponse> subs = new Vector<>();
    // instead of a final list, im gonna try a map to keep track of the subscribed clients AND their threshold values
    private static Map<AsyncResponse, ConcertInfoSubscriptionDTO> subs = new ConcurrentHashMap<>();

    // Subscribes to notification alert when the concert threshold is reached.
    // Note that a user must be authenticated in order to subscribe.
    @POST
    @Path("/subscribe/concertInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response subscribeToNotification(
        ConcertInfoSubscriptionDTO subscriptionRequestDTO,
        @Suspended AsyncResponse response,
        @CookieParam("auth") Cookie auth) {
            
            // first we want to check if the user is authenticated
            if (auth == null) {
                return Response.status(Status.UNAUTHORIZED).build();
            }

            // if user is authenticated, we proceed with checking if the concert and date is valid.
        
            // we need to check whether the requested concert and date exist.
            EntityManager em = PersistenceManager.instance().createEntityManager();
            try {
                em.getTransaction().begin();

                // check if the concert exists.
                Concert concert = em.find(Concert.class, subscriptionRequestDTO.getConcertId());
                if (concert == null) {
                    return Response.status(Status.BAD_REQUEST).entity("Concert not found").build();
                }
                                
                // check if the date is valid (that is, if concert date in request is in fact a date in that concert schedule)
                Set<LocalDateTime> dateList = new HashSet<>();
                dateList = concert.getDates();
                // check if the requested concert's date is in the concerts schedule.
                if (dateList.contains(subscriptionRequestDTO.getDate())) {
                    return Response.status(Status.BAD_REQUEST).entity("Date not valid").build();
                }

                // if these checks pass, we can update and store the subscription.
                subs.put(response, subscriptionRequestDTO);
                
                // get the threshold we want to check from the subscription request
                //int requiredPercentageBooked = subscriptionRequestDTO.getPercentageBooked();
                //int actualPercentageBooked = getPercentageBookedForConcert(subscriptionRequestDTO.getConcertId(), subscriptionRequestDTO.getDate());
                //ConcertInfoNotificationDTO notification; 

                // we want to check if the percentage of booked seats is greater than the required percentage as requested by the subscriber
                // we then want to return a response to the client.
                //if (actualPercentageBooked >= requiredPercentageBooked) {
                //    notification.setNumSeatsRemaining(actualPercentageBooked);
                //    response.resume(notification);  // notify the client asynchronously.
                //}    

            } catch (NoResultException e) {
                return Response.status(Status.BAD_REQUEST).build();
            } finally {
                em.close(); // last thing to do is to close the persistence manager.
            }
            
            return Response.ok().build();
    }

    //private int getPercentageBookedForConcert(long concertId, LocalDateTime date) {
        // method to get how many seats are booked for a concert on a given date.
        
    //}

    // need a method to send notifications to subscribed clients
    
}
