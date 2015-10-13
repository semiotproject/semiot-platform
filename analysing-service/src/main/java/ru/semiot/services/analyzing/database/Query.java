package ru.semiot.services.analyzing.database;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Entity
@Table(name = "queries")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Query.findAll", query = "SELECT q FROM Query q"),
    @NamedQuery(name = "Query.findById", query = "SELECT q FROM Query q WHERE q.id = :id"),
    @NamedQuery(name = "Query.count", query = "SELECT COUNT(*) FROM Query"),
    @NamedQuery(name = "Query.getLastID", query = "SELECT MAX(q.id) FROM Query q")})
public class Query implements Serializable {

    private static final long serialVersionUID = 1L;
    @Lob
    @Size(max = 65535)
    @Column(name = "name")
    private String name;
    @Lob
    @Size(max = 65535)
    @Column(name = "query")
    private String query;
    @Lob
    @Size(max = 65535)
    @Column(name = "time")
    private String time;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;

    public Query() {

    }

    public Query(String text, String name, int id) {
        this.id = id;
        this.time = Long.toString(new Date().getTime());
        this.name = name;
        this.query = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Query)) {
            return false;
        }
        Query other = (Query) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{\"id\": \"" + id + "\",\n\"created\": \"" + time + "\",\n\"name\": \"" + name + "\",\n\"text\": \"" + query + "\"}";
    }

}
