package ru.semiot.web;

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
        em.persist(new Request(id, request));
    }

    @Override
    public int getCount() {
        return em.createNamedQuery("Requests.getAll",Request.class).getResultList().size();
    }

    @Override
    public String getRequest(int id) {
        return em.find(Request.class, id).getRequest();
    }
    
    private Request getEntity(int id){
        return em.find(Request.class, id);
    }
    @Override
    public void removeRequest(int id) {
        em.remove(getEntity(id));
    }
    
}
