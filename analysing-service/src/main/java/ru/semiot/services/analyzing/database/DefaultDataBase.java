package ru.semiot.services.analyzing.database;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class DefaultDataBase implements DataBase{
    @PersistenceContext(unitName = "DataSource")
    private EntityManager em;
    
    @Override
    public void appendQuery(String request) {
        List l = em.createNamedQuery("Requests.getAll",Query.class).getResultList();
        int id;
        if(l == null || l.isEmpty())
            id=0;
        else
            id = ((Query)l.get(l.size()-1)).getId();
        Query r = new Query(++id, request);
        em.persist(r);
    }

    @Override
    public int getCount() {
        return em.createNamedQuery("Requests.getAll",Query.class).getResultList().size();
    }

    @Override
    public String getQuery(int id) {
        Query r = em.find(Query.class, id);
        if(r!=null)
            return r.getRequest();
        else
            return null;
    }
    
    private Query getEntity(int id){
        return em.find(Query.class, id);
    }
    @Override
    public String removeQuery(int id) {
        Query r = getEntity(id);
        if(r != null){
            em.remove(getEntity(id));
            return r.getRequest();
        }
        else
            return null;
    }

    @Override
    public String [] getQueries() {
        List l = em.createNamedQuery("Requests.getAll",Query.class).getResultList();
        if(l == null || l.isEmpty())
            return null;
        String [] queries = new String [l.size()];
        int i = 0;
        for (Object q : l) {
            queries[i++] = ((Query)q).getRequest();
        }
        return queries;
    }

    @Override
    public boolean removeQueries() {
        try{
            em.createNamedQuery("Requests.removeAll").executeUpdate();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
}
