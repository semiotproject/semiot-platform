package ru.semiot.services.analyzing.database;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

@Entity(name = "Requests")
@NamedQueries({
@NamedQuery(name = "Requests.getAll", query = "SELECT r FROM Requests r"),
@NamedQuery(name = "Requests.removeAll", query = "DELETE FROM Requests WHERE id > -1")
})
public class Query implements Serializable {

    @Id
    @NotNull
    @Column(name = "id")
    private int id;

    @Column(name = "Request")
    private String request;

    public Query() {
    }

    public Query(int id, String request) {
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
