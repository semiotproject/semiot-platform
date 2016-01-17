package ru.semiot.platform.drivers.netatmo.temperature;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import static org.junit.Assert.*;

public class NetAtmoAPITest {
    
    private final static String CLIENT_APP_ID = "566ffb00e8ede139a1529208";
    private final static String CLIENT_SECRET = "C5hws3Fo0fG0wiZpdd1AKi64OTZK7G3Ic0eu5R";
    private final static String USERNAME = "garayzuev@gmail.com";
    private final static String PASSWORD = "Ny7!Ze#o";
    
    private static final String LAT_SW = "60.008394"; 
    private static final String LON_SW = "30.260427";
    private static final String LAT_NE = "60.015601";
    private static final String LON_NE = "30.297721";
    
    @Test
    public void testAuthenticate() {
        NetatmoAPI api = new NetatmoAPI(CLIENT_APP_ID, CLIENT_SECRET);
        
        assertFalse(api.authenticate("balhbala", "bahad"));
        
        assertTrue(api.authenticate(USERNAME, PASSWORD));
    }
    
    @Test
    public void testGetDevices() throws JSONException {
        NetatmoAPI api = new NetatmoAPI(CLIENT_APP_ID, CLIENT_SECRET);        
        api.authenticate(USERNAME, PASSWORD);
        
        JSONArray devices = api.getPublicData(LAT_NE, LON_NE, LAT_SW, LON_SW);
        
        System.out.println(devices);
        
        assertNotNull(devices);
    }
    
}
