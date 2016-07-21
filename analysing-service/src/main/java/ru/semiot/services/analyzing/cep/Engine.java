package ru.semiot.services.analyzing.cep;


public interface Engine {
    public void appendData(String message);
    public boolean registerQuery(int query_id);
    public void removeQuery(int query_id);    
}
