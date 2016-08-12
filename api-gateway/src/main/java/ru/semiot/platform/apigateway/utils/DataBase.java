package ru.semiot.platform.apigateway.utils;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Stateless
public class DataBase {

  @PersistenceContext(unitName = "DataSource")
  private EntityManager em;

  public Credentials addUser(int id, String login, String password, String role) {
      return addUser(new Credentials(id, login, password, role));
  }

  public Credentials addUser(Credentials c) {
    try {
      em.merge(c);
      return c;
    } catch (Exception ex) {
      return null;
    }
  }

  public boolean isUniqueLogin(String login, int id) {
    try {
      return getUser(login).getId() == id;
    } catch (Exception ex) {
      return true;
    }
  }

  public boolean updateUser(int id, String login, String password, String role) {
    try {
      Credentials user = em.find(Credentials.class, id);
      user.updateInfo(login, password, role);
      em.merge(user);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public boolean updateUser(Credentials user) {
    try {
      return updateUser(user.getId(), user.getLogin(), user.getPassword(), user.getRole());
    } catch (Exception ex) {
      return false;
    }
  }

  public boolean removeUser(int id) {
    try {
      em.remove(em.find(Credentials.class, id));
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public List<Credentials> getAllUsers() {
    return em.createNamedQuery("Credentials.findAll").getResultList();
  }

  public Credentials getUser(String login) {
    try {
      return ((Credentials) em.createNamedQuery("Credentials.findByLogin")
          .setParameter("login", login).getSingleResult());
    } catch (Exception ex) {
      return null;
    }
  }
}
