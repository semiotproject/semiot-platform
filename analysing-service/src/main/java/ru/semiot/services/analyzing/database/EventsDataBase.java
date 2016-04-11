package ru.semiot.services.analyzing.database;

import org.json.JSONArray;
import org.json.JSONObject;

public interface EventsDataBase {
    public void appendEvents(int query_id, String events);
    public JSONArray getEventsByQueryId(int query_id);
    public JSONObject getEventsById(int id);
    public JSONObject removeEventsById(int id);
    public JSONArray removeEventsByQueryId(int query_id);
    public JSONArray getEventsByTime(long start, long end, int query_id);
    public long getCount();
}
