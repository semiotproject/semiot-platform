package ru.semiot.services.analyzing.database;

import org.json.JSONObject;

public interface EventsDataBase {
    public void appendEvents(int query_id, String events);
    public JSONObject getEventsFromQuery(int query_id);
    public JSONObject getEventsFromID(int id);
    public JSONObject removeEvents(int id);
    public long getCount();
}
