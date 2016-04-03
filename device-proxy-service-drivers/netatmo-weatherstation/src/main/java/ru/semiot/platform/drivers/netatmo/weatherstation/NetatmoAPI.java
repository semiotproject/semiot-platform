package ru.semiot.platform.drivers.netatmo.weatherstation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetatmoAPI {

    private static final Logger logger = LoggerFactory.getLogger(NetatmoAPI.class);
    private static final String AUTH_API_URI = "https://api.netatmo.net/oauth2/token";
    private static final String PUBLIC_DATA_API_URI = "https://api.netatmo.com/api/getpublicdata";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String GRANT_TYPE_PASSWORD_VALUE = "password";
    private static final String GRANT_TYPE_REFRESH_TOKEN_VALUE = "refresh_token";
    private static final String CLIENT_APP_ID_KEY = "client_id";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String SCOPE_KEY = "scope";
    private static final String SCOPE_VALUE = "read_station read_thermostat";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String LATITUDE_NORTH_EAST_KEY = "lat_ne";
    private static final String LONGITUDE_NORTH_EAST_KEY = "lon_ne";
    private static final String LATITUDE_SOUTH_WEST_KEY = "lat_sw";
    private static final String LONGITUDE_SOUTH_WEST_KEY = "lon_sw";
    private static final String ERROR_KEY = "error";
    private static final String ERROR_INVALID_GRANT = "invalid grant";
    private static final String BODY_KEY = "body";

    private final String client_app_id;
    private final String client_secret;

    private String access_token;
    private String refresh_token;

    public NetatmoAPI(String client_app_id, String client_secret) {
        this.client_app_id = client_app_id;
        this.client_secret = client_secret;
    }

    public boolean authenticate(String username, String password) {
        return _authenticate(username, password);
    }

    private boolean _authenticate(String username, String password) {
        try {
            List<NameValuePair> form = Form.form()
                    .add(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD_VALUE)
                    .add(CLIENT_APP_ID_KEY, client_app_id)
                    .add(CLIENT_SECRET_KEY, client_secret)
                    .add(USERNAME_KEY, username)
                    .add(PASSWORD_KEY, password)
                    .add(SCOPE_KEY, SCOPE_VALUE)
                    .build();
            Request request = Request.Post(AUTH_API_URI).bodyForm(form);
            
            HttpResponse response = request.execute().returnResponse();

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                JSONObject obj = new JSONObject(
                        EntityUtils.toString(response.getEntity()));

                access_token = obj.getString(ACCESS_TOKEN_KEY);
                refresh_token = obj.getString(REFRESH_TOKEN_KEY);

                return true;
            } else {
                logger.debug("Response: status [{}], content [{}], request [{}], form [{}]", 
                        response.getStatusLine().getStatusCode(),
                        EntityUtils.toString(response.getEntity()),
                        request.toString(), Arrays.toString(form.toArray()));
                
                return false;
            }
        } catch (IOException | JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        }

        return false;
    }

    private boolean _authenticate() {
        try {
            if (refresh_token != null) {
                HttpResponse response = Request.Post(AUTH_API_URI).bodyForm(
                        Form.form()
                        .add(GRANT_TYPE_KEY, GRANT_TYPE_REFRESH_TOKEN_VALUE)
                        .add(CLIENT_APP_ID_KEY, client_app_id)
                        .add(CLIENT_SECRET_KEY, client_secret)
                        .add(REFRESH_TOKEN_KEY, refresh_token)
                        .add(SCOPE_KEY, SCOPE_VALUE)
                        .build())
                        .execute().returnResponse();

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject obj = new JSONObject(
                            EntityUtils.toString(response.getEntity()));

                    access_token = obj.getString(ACCESS_TOKEN_KEY);
                    refresh_token = obj.getString(REFRESH_TOKEN_KEY);

                    return true;
                }
            }
        } catch (IOException | JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        }

        return false;
    }

    /**
     * See also <a href="https://dev.netatmo.com/doc/methods/getpublicdata">
     * https://dev.netatmo.com/doc/methods/getpublicdata</a>.
     * 
     * @param lat_ne
     * @param lon_ne
     * @param lat_sw
     * @param lon_sw
     * @return
     * @throws JSONException 
     */
    public JSONArray getPublicData(String lat_ne, String lon_ne,
            String lat_sw, String lon_sw) 
            throws JSONException {
        if(isEmpty(lat_ne, lon_ne, lat_sw, lon_sw)) {
            logger.warn("Empty area values: lat_ne: {}, lon_ne: {}, lat_sw: {}, lon_sw: {}", lat_ne, lon_ne, lat_sw, lon_sw);
            return null;
        }
        
        try {
            final URI uri = new URIBuilder(PUBLIC_DATA_API_URI)
                    .addParameter(ACCESS_TOKEN_KEY, access_token)
                    .addParameter(LATITUDE_NORTH_EAST_KEY, lat_ne)
                    .addParameter(LONGITUDE_NORTH_EAST_KEY, lon_ne)
                    .addParameter(LATITUDE_SOUTH_WEST_KEY, lat_sw)
                    .addParameter(LONGITUDE_SOUTH_WEST_KEY, lon_sw)
                    .build();

            HttpResponse response = Request.Get(uri)
                    .execute()
                    .returnResponse();

            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                return new JSONObject(EntityUtils.toString(response.getEntity()))
                        .getJSONArray(BODY_KEY);
            } else {
                if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                    logger.warn(
                            "Failed to \"getpublicdata\"! URL: {}, Status: {}, Message: {}.", 
                            uri, response.getStatusLine(), EntityUtils.toString(response.getEntity()));
                    try {
                        JSONObject msg = new JSONObject(
                                EntityUtils.toString(response.getEntity()));

                        if (msg.optString(ERROR_KEY).equals(ERROR_INVALID_GRANT)) {
                            logger.debug("Looks like access_token expired. Refreshing.");
                            
                            if(_authenticate()) {
                                logger.debug("Successfully refreshed access_token");
                                return getPublicData(lat_ne, lon_ne, lat_sw, lon_sw);
                            } else {
                                logger.warn("Failed to refresh access_token!");
                            }
                        }
                    } catch (JSONException ex) {
                        logger.warn(ex.getMessage(), ex);
                    }
                }
                else{
                    logger.warn("Failed to \"getpublicdata\"! Unrecognized response code. URL: {}, Status: {}, Message: {}.", 
                            uri, response.getStatusLine(), EntityUtils.toString(response.getEntity()));
                }
            }
        } catch (IOException | URISyntaxException ex) {
            logger.warn(ex.getMessage(), ex);
        }

        return null;
    }
    
    private boolean isEmpty(String... strings) {
        for(String str : strings) {
            if(str == null || str.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
}
