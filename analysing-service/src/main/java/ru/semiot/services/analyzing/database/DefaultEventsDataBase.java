package ru.semiot.services.analyzing.database;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.json.JSONArray;
import org.json.JSONObject;

@Stateless
public class DefaultEventsDataBase implements EventsDataBase {

    @PersistenceContext(unitName = "DataSource")
    private EntityManager em;
    private static int counter = 0;
    private static final int EVENTS = 288;
    private static final int MAX_EVENTS_FOR_QUERY = 1000;

    @Override
    public void appendEvents(int query_id, String events) {
        int id = query_id * MAX_EVENTS_FOR_QUERY + counter;
        Events e;
        try {
            e = em.createQuery("SELECT e FROM Events e WHERE e.id = :id", Events.class).setParameter("id", id).getSingleResult();
            e.setEvents(events);

        } catch (NoResultException ex) {
            Query q = em.createNamedQuery("Query.findById", Query.class).setParameter("id", query_id).getSingleResult();
            e = new Events(id, q, events);
        }
        em.merge(e);
        em.flush();
        if (counter < EVENTS) {
            counter++;
        } else {
            counter = 0;
        }
    }

    @Override
    public JSONArray getEventsByQueryId(int query_id) {
        try {
            Query q = em.createNamedQuery("Query.findById", Query.class).setParameter("id", query_id).getSingleResult();
            Collection<Events> list = q.getEventsCollection();
            if(list == null)
                return null;            
            JSONArray arr = new JSONArray();            
            for (Events e : list) {
                arr.put(new JSONObject(e.toString()));
            }
            return arr;
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public JSONObject getEventsById(int id) {
        Events e = em.find(Events.class, id);
        if (e != null) {
            JSONObject object = new JSONObject(e.toString());
            return object;
        }
        return null;
    }

    @Override
    public JSONObject removeEventsById(int id) {
        Events e = em.find(Events.class, id);
        if (e != null) {
            em.remove(e);
            return new JSONObject(e.toString());
        } else {
            return null;
        }
    }

    @Override
    public long getCount() {
        return (Long) em.createQuery("SELECT COUNT(e.id) FROM Events e").getSingleResult();
    }

    @Override
    public JSONArray removeEventsByQueryId(int query_id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONArray getEventsByTime(long start, long end, int query_id) {
        try {
            Query q = em.createNamedQuery("Query.findById", Query.class).setParameter("id", query_id).getSingleResult();
            JSONArray arr = new JSONArray();
            List<Events> list = em.createNamedQuery("Events.findByTime", Events.class).setParameter("st_time", new Date(start)).setParameter("end_time", new Date (end)).setParameter("id", q).getResultList();
            if(list == null)
                return arr;
            for (Events e : list) {
                arr.put(new JSONObject(e.toString()));
            }
            return arr;
        }catch (NoResultException ex) {
            System.out.println("start " + start + " end " + end);
            return null;
        }
    }

}
