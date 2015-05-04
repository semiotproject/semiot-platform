package ru.semiot.services.analyzing.rest;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Stateless
public class DefaultDataBase implements DataBase{
    @PersistenceContext(unitName = "WebPU")
    private EntityManager em;
    
    @Override
    public void appendRequest(String request) {
        int id = getCount();
        Request r = new Request(id, request);
        em.persist(r);
    }

    @Override
    public int getCount() {
        return em.createNamedQuery("Requests.getAll",Request.class).getResultList().size();
    }

    @Override
    public String getRequest(int id) {
        Request r = em.find(Request.class, id);
        if(r!=null)
            return r.getRequest();
        else
            return "Not found";
    }
    
    private Request getEntity(int id){
        return em.find(Request.class, id);
    }
    @Override
    public String removeRequest(int id) {
        Request r = getEntity(id);
        if(r != null){
            em.remove(getEntity(id));
            return r.getRequest();
        }
        else
            return "Not found";
    }
    
}
