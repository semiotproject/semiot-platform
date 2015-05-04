package ru.semiot.services.analyzing.database;

public interface DataBase {
    public void appendQuery(String request);
    public int getCount();
    public String getQuery(int id);
    public String [] getQueries();
    public String removeQuery(int id);
    public boolean removeQueries();
}
