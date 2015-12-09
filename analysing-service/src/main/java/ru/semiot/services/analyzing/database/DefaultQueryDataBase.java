package ru.semiot.services.analyzing.database;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.json.JSONArray;
import org.json.JSONObject;

@Stateless
public class DefaultQueryDataBase implements QueryDataBase {

    @PersistenceContext(unitName = "DataSource")
    private EntityManager em;

    @Override
    public JSONObject appendQuery(String request, String name) {
        //Bad, bad function
        int id = 0;
        if (getCount() > 0) {
            id = (Integer) em.createQuery("SELECT MAX(q.id) FROM Query q").getSingleResult();
        }
        Query r = new Query(request, name, ++id);
        em.merge(r);
        return new JSONObject(r.toString());
    }

    @Override
    public long getCount() {
        return (Long) em.createQuery("SELECT COUNT(q.id) FROM Query q").getSingleResult();
    }

    @Override
    public JSONObject getQuery(int id) {
        Query r = em.find(Query.class, id);
        if (r != null) {
            JSONObject object = new JSONObject(r.toString());
            return object;
        } else {
            return null;
        }
    }

    private Query getEntity(int id) {
        return em.find(Query.class, id);
    }

    @Override
    public JSONObject removeQuery(int id) {
        Query r = getEntity(id);
        if (r != null) {
            em.remove(getEntity(id));
            return new JSONObject(r.toString());
        } else {
            return null;
        }
    }

    @Override
    public JSONArray getQueries() {
        List l = em.createNamedQuery("Query.findAll", Query.class).getResultList();
        if (l == null || l.isEmpty()) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (Object q : l) {
            array.put(new JSONObject(((Query) q).toString()));
        }
        return array;
    }

    @Override
    public boolean removeQueries() {
        try {
            //em.createNamedQuery("Requests.removeAll").executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
