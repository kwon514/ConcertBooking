package proj.concert.service.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.core.GenericEntity;


public class Services {

    // generify
    public static <T> GenericEntity<List<T>> generify(List<T> objects) {
        return new GenericEntity<List<T>>(objects) {};
    }

    // get single object of type T
    public static <T> T get(Class<T> type, long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        T object = em.find(type, id);
        em.close();
        return object;
    }

    // get all objects of type T
    public static <T> List<T> getAll(Class<T> type) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        List<T> objects = em
                .createQuery(String.format("SELECT e FROM %s e", type.getSimpleName()), type)
                .getResultList();
        em.close();
        return objects;
    }

}
