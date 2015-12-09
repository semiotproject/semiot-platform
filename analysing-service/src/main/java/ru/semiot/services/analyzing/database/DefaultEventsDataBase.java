package ru.semiot.services.analyzing.database;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.json.JSONObject;

@Stateless
public class DefaultEventsDataBase implements EventsDataBase {

    @PersistenceContext(unitName = "DataSource")
    private EntityManager em;

    @Override
    public void appendEvents(int query_id, String events) {
        Events e;
        try {
            e = em.createNamedQuery("Events.findByQueryId", Events.class).setParameter("queryId", query_id).getSingleResult();
            e.setEvents(events);
        } catch (NoResultException ex) {
            int id = 0;
            if (getCount() > 0) {
                id = (Integer) em.createQuery("SELECT MAX(e.id) FROM Events e").getSingleResult();
            }
            e = new Events(++id, query_id, events);
        }

        em.merge(e);
    }

    @Override
    public JSONObject getEventsFromQuery(int query_id) {
        try {
            Events e = em.createNamedQuery("Events.findByQueryId", Events.class).setParameter("queryId", query_id).getSingleResult();
            JSONObject object = new JSONObject(e.toString());
            return object;
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public JSONObject getEventsFromID(int id) {
        Events e = em.find(Events.class, id);
        if (e != null) {
            JSONObject object = new JSONObject(e.toString());
            return object;
        }
        return null;
    }

    @Override
    public JSONObject removeEvents(int id) {
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

}
