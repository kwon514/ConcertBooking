package proj.concert.service.services;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Performer;
import proj.concert.service.domain.User;


@Path("/concert-service")
public class ConcertResource {

    // private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

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

        return concert != null ? Response.ok(concert).build() : Response.status(Response.Status.NOT_FOUND).build();
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

        return concerts != null ? Response.ok(concerts).build() : Response.status(Response.Status.NO_CONTENT).build();
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

        return concerts != null ? Response.ok(concerts).build() : Response.status(Response.Status.NO_CONTENT).build();
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

        return performer != null ? Response.ok(performer).build() : Response.status(Response.Status.NOT_FOUND).build();
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

        return performers != null ? Response.ok(performers).build()
                : Response.status(Response.Status.NO_CONTENT).build();
    }

    // ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response authUser(User user) {
        User u;
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            u = em.createQuery("SELECT u FROM User u WHERE u.username = ?1 AND u.password = ?2", User.class)
                    .setParameter(1, user.getUsername())
                    .setParameter(2, user.getPassword())
                    .getSingleResult();
            em.getTransaction().commit();
        }
        catch (NoResultException e) { u = null; }
        finally { em.close(); }

        return u != null ? Response.ok().cookie(getCookie(u)).build()
                         : Response.status(Status.UNAUTHORIZED).build();
    }

    private NewCookie getCookie(User user) {
        return new NewCookie("auth", Integer.toString(user.hashCode()));
    }

}
