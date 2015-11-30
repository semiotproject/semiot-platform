package ru.semiot.services.analyzing.cep;


public interface Engine {
    public void appendData(String message);
    public boolean registerQuery(String query);
    public void removeQuery(String query);    
}
