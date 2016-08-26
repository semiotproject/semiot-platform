package ru.semiot.services.analyzing.cep;

import com.hp.hpl.jena.rdf.model.Model;


public interface Engine {
    public void appendData(Model description);
    public boolean registerQuery(int query_id);
    public void removeQuery(int query_id);
}
