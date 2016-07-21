package ru.semiot.services.analyzing.database;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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
    @NamedQuery(name = "Query.count", query = "SELECT COUNT(q.id) FROM Query q"),
    @NamedQuery(name = "Query.getLastID", query = "SELECT MAX(q.id) FROM Query q")})
public class Query implements Serializable {
    @Lob
    @Size(max = 65535)
    @Column(name = "sparql")
    private String sparql;

    @Basic(optional = false)
    @NotNull
    @Column(name = "time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;
    @OneToMany(mappedBy = "queryId")
    private Collection<Events> eventsCollection;

    private static final long serialVersionUID = 1L;
    @Lob
    @Size(max = 65535)
    @Column(name = "name")
    private String name;
    @Lob
    @Size(max = 65535)
    @Column(name = "query")
    private String query;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;

    public Query() {

    }

    public Query(String text, String name, String sparql, int id) {
        this.id = id;
        this.time = new Date();
        this.name = name;
        this.query = text;
        this.sparql = sparql;
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
        return "{\"id\": \"" + id + "\",\n\"created\": \"" + time.getTime() + "\",\n\"name\": \"" + name.replace("\"", "\\\"") + "\",\n\"text\": \"" + query.replace("\"", "\\\"") + "\",\n\"sparql\": \"" + sparql.replace("\"", "\\\"")+ "\"}";
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @XmlTransient
    public Collection<Events> getEventsCollection() {
        return eventsCollection;
    }

    public void setEventsCollection(Collection<Events> eventsCollection) {
        this.eventsCollection = eventsCollection;
    }

    public String getSparql() {
        return sparql;
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }
}
