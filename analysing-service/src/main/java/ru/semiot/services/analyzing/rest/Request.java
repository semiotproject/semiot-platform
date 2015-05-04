package ru.semiot.services.analyzing.rest;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Entity(name = "Requests")
@NamedQuery(name = "Requests.getAll", query = "SELECT r FROM Requests r")
public class Request implements Serializable {
    @Id
    @NotNull
    @Column(name = "id")
    private int id;
    
    @Column(name = "Request")
    private String request;

    public Request() {
    }

    public Request(int id, String request) {
        this.id = id;
        this.request = request;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    @Override
    public String toString() {
        return "Id is " + id + "Request is " + request;
    }
    
}
