package ru.semiot.services.analyzing.database;

import org.json.JSONArray;
import org.json.JSONObject;

public interface QueryDataBase {
    public JSONObject appendQuery(String request, String name, String sparql);
    public long getCount();
    public JSONObject getQuery(int id);
    public JSONArray getQueries();
    public JSONObject removeQuery(int id);
    public boolean removeQueries();
}
