package proj.concert.service.services;

import proj.concert.service.domain.*;
import proj.concert.service.mapper.*;
import static proj.concert.service.services.Services.*;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;


@Produces(MediaType.APPLICATION_JSON)
@Path("/concert-service")
public class ConcertResource {

    // private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);


    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    // CONCERTS
    // _________________________________________________________________________

    // get single concert
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/concerts/{id}")
    public Response getSingleConcert(@PathParam("id") long id) {
        Concert concert = get(Concert.class, id);

        return concert == null ?
                Response.status(Response.Status.NOT_FOUND).build() :
                Response.ok(ConcertMapper.toConcertDTO(concert)).build();
    }

    // get all concerts
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/concerts")
    public Response getAllConcerts() {
        List<Concert> concerts = getAll(Concert.class);

        return concerts.isEmpty() ?
                Response.status(Response.Status.NO_CONTENT).build() :
                Response.ok(generify(ConcertMapper.toConcertDTOs(concerts))).build();
    }

    // get concert summaries
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/concerts/summaries")
    public Response getConcertSummaries() {
        List<Concert> concerts = getAll(Concert.class);

        return concerts.isEmpty() ?
                Response.status(Response.Status.NO_CONTENT).build() :
                Response.ok(generify(ConcertMapper.toConcertSummaryDTOs(concerts))).build();
    }


    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    // PERFORMERS
    // _________________________________________________________________________

    // get single performer
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/performers/{id}")
    public Response getSinglePerformer(@PathParam("id") long id) {
        Performer performer = get(Performer.class, id);

        return performer == null ?
                Response.status(Response.Status.NOT_FOUND).build() :
                Response.ok(ConcertMapper.toPerformerDTO(performer)).build();
    }

    // get all performers
    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
    @GET
    @Path("/performers")
    public Response getAllPerformers() {
        List<Performer> performers = getAll(Performer.class);

        return performers.isEmpty() ?
                Response.status(Status.NO_CONTENT).build() :
                Response.ok(generify(ConcertMapper.toPerformerDTOs(performers))).build();
    }

}
