package ru.semiot.platform.drivers.netatmo.temperature;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class ScheduledDevice implements Runnable {

    private final static Logger logger = Logger.getLogger(ScheduledDevice.class);
    private final DeviceDriverImpl ddi;

    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;

    private final static String CLIENT_ID = "566ffb00e8ede139a1529208";
    private final static String CLIENT_SECRET = "C5hws3Fo0fG0wiZpdd1AKi64OTZK7G3Ic0eu5R";
    private final static String USERNAME = "garayzuev@gmail.com";
    private final static String PASSWORD = "Ny7!Ze#o";
    private final static String ACCESS_URI = "https://api.netatmo.net/oauth2/token";
    private final static String DATA_URI = "https://api.netatmo.com/api/getpublicdata";
    private final static String BODY_TEMPLATE = "grant_type=password&"
            + "client_id=${CLIENT_ID}&"
            + "client_secret=${CLIENT_SECRET}&"
            + "username=${USERNAME}&"
            + "password=${PASSWORD}&"
            + "scope=read_station read_thermostat";
    private String access_token = null;
    private final static SSLConnectionSocketFactory sslf = initSSL();
    private String lat_sw, lon_sw, lat_ne, lon_ne;

    public void setGeoPosition(double lat_sw, double lon_sw, double lat_ne, double lon_ne) {
        this.lat_sw = String.valueOf(lat_sw);
        this.lat_ne = String.valueOf(lat_ne);
        this.lon_sw = String.valueOf(lon_sw);
        this.lon_ne = String.valueOf(lon_ne);
    }
    private static final String templateTopic = "${DEVICE_HASH}";
    private static final String templateOnState = "prefix saref: <http://ontology.tno.nl/saref#> "
            + "<http://${DOMAIN}/${SYSTEM_PATH}/${DEVICE_HASH}> saref:hasState saref:OnState.";

    /* String templateSubsystem = "ssn:hasSubSystem [ a ssn:SensingDevice ; "
     + "ssn:observes qudt-quantity:ThermodynamicTemperature ; ssn:hasMeasurementCapability ["
     + " a ssn:MeasurementCapability ; ssn:forProperty qudt-quantity:ThermodynamicTemperature ; "
     + "ssn:hasMeasurementProperty [ a qudt:Unit ; ssn:hasValue [ a qudt:Quantity ; "
     + "ssn:hasValue qudt-unit:DegreeCelsius ; ] ; ] ; ] ; ]"; */
    public ScheduledDevice(DeviceDriverImpl ddi) {
        this.ddi = ddi;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        rootLogger.setLevel(Level.ALL);
        ScheduledDevice sd = new ScheduledDevice(null);

        sd.run();

    }

    public void run() {
        logger.debug("Run method started");
        long currentTimestamp = System.currentTimeMillis();
        try {
            List<Device> list = new ArrayList<>();

            JSONArray devices = getData();
            logger.debug("Data are returned");
            if (devices == null || devices.length() == 0) {
                logger.warn("Devices are empty!");
                return;
            }
            logger.debug("Extract data");
            for (int i = 0; i < devices.length(); i++) {
                JSONObject device = devices.getJSONObject(i);
                String hash = getHash(device.getString("_id"));
                String lon = String.valueOf(device.getJSONObject("place").getJSONArray("location").get(0));
                String lat = String.valueOf(device.getJSONObject("place").getJSONArray("location").get(1));
                String value = "";
                JSONObject measures = device.getJSONObject("measures");
                boolean foundValue = false;
                Iterator it = measures.keys();
                while (it.hasNext() && !foundValue) {
                    //Показания могут быть с нескольких сенсоров, поэтому ищем тот сенсор, который снимает температурные показания
                    JSONObject sensor = measures.getJSONObject((String) it.next());
                    if (sensor.has("type")) {
                        JSONArray types = sensor.getJSONArray("type");
                        int index = -1;
                        for (int j = 0; j < types.length(); j++) {
                            if (types.getString(j).equals("temperature")) {
                                index = j;//Поскольку температурные показания хранятся вместе с влажностью, то устанавливаем индекс температуры
                                foundValue = true;
                                break;
                            }
                        }
                        if (foundValue) {
                            //Теперь найдем последнее показания температурного сенсора
                            JSONObject results = sensor.getJSONObject("res");
                            long lastObservation = 0;
                            long z;
                            Iterator iter = results.keys();
                            while (iter.hasNext()) {
                                z = Long.parseLong((String) iter.next());
                                lastObservation = lastObservation < z ? z : lastObservation;
                            }
                            if (lastObservation != 0) {
                                value = String.valueOf(results.getJSONArray(Long.toString(lastObservation)).get(index));
                            } else {
                                foundValue = false;
                            }
                        }
                    }
                }
                if (!foundValue) {
                    continue;
                }
                Device _device = new Device(hash, "");
                list.add(_device);
                logger.info(hash + " " + value);
                if (ddi.contains(_device)) {
                    int index = ddi.listDevices().indexOf(_device);
                    if (index >= 0) {
                        Device deviceOld = ddi.listDevices().get(index);
                        if (deviceOld != null && !deviceOld.getTurnOn()) {
                            deviceOld.setTurnOn(true);
                            ddi.inactiveDevice(templateOnState
                                    .replace("${DEVICE_HASH}", deviceOld.getID())
                                    .replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
                                    .replace("${DOMAIN}", ddi.getDomain()));
                        }
                    }
                    sendMessage(value, currentTimestamp, hash);
                } else {
                    _device.setRDFDescription(ddi.getTemplateDescription()
                            .replace("${DEVICE_HASH}", hash)
                            .replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
                            .replace("${SENSOR_PATH}", ddi.getPathSensorUri())
                            .replace("${SENSOR_ID}", "1")
                            .replace("${DOMAIN}", ddi.getDomain())
                            .replace("${LATITUDE}", lat)
                            .replace("${LONGITUDE}", lon));
                    ddi.addDevice(_device);
                    sendMessage(value, currentTimestamp, hash);
                }
            }
            for (Device dev : ddi.listDevices()) {
                if (!list.contains(dev) && dev.getTurnOn()) {
                    dev.setTurnOn(false);
                    ddi.inactiveDevice(DeviceDriverImpl.templateOffState
                            .replace("${DEVICE_HASH}", dev.getID())
                            .replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
                            .replace("${DOMAIN}", ddi.getDomain()));
                }
            }
        } catch (JSONException | NumberFormatException ex) {
            logger.error("Something went wrong! " + ex.getMessage());
        }
    }

    private static SSLConnectionSocketFactory initSSL() {
        logger.debug("SSL is initializing");
        //Trust everyone
        return new SSLConnectionSocketFactory(SSLContexts.createDefault(), 
                NoopHostnameVerifier.INSTANCE);
    }

    private void initCredential() {
        logger.debug("Initialize Credential");
        try {
            logger.debug("Init URI");
            final URI uri = new URI(ACCESS_URI);
            HttpPost post = new HttpPost(uri);
            logger.debug("Setting Entity");
            post.setEntity(new StringEntity(
                    BODY_TEMPLATE
                    .replace("${CLIENT_ID}", CLIENT_ID)
                    .replace("${CLIENT_SECRET}", CLIENT_SECRET)
                    .replace("${USERNAME}", USERNAME)
                    .replace("${PASSWORD}", PASSWORD),
                    ContentType.APPLICATION_FORM_URLENCODED));
            logger.debug("Init Client");
            CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslf).build();
//            CloseableHttpClient cl = HttpClients.createDefault();
            HttpGet q = new HttpGet("http://www.google.com");
            logger.debug("Client is initialized! client=" + client.toString() + " SSLSocketFactory is " + sslf.toString());
            logger.debug(post.toString() + ". Body is " + EntityUtils.toString(post.getEntity()) + ". URI is " + post.getURI().toString());
            HttpResponse response = client.execute(post);
            logger.debug("Post query is executed");
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.warn("Credential data are wrong!");
                return;
            }
            access_token = new JSONObject(EntityUtils.toString(response.getEntity())).getString("access_token");
            logger.debug("Access token is got! It is " + access_token);

        } catch (Exception ex) {
            logger.error("Something went wrong! " + ex.getMessage());
        }
    }

    private void initGeo() {
        setGeoPosition(ddi.getLat_sw(), ddi.getLon_sw(), ddi.getLat_ne(), ddi.getLon_ne());
        logger.debug("GEO is (lat,lon): sw(" + lat_sw + "," + lon_sw + "); ne(" + lat_ne + "," + lon_ne + ")");
    }

    private JSONArray getData() {
        logger.debug("Try to get data");
        //initGeo();//Метод необходим для запуска через felix
        setGeoPosition(59.71779, 29.750763, 60.138258, 30.677734);//Метод нужен для локальной проверки класса
        try {
            initCredential();
            logger.debug("Building URI");
            final URI uri = new URIBuilder(DATA_URI)
                    .addParameter("access_token", access_token)
                    .addParameter("lat_ne", lat_ne)
                    .addParameter("lon_ne", lon_ne)
                    .addParameter("lat_sw", lat_sw)
                    .addParameter("lon_sw", lon_sw)
                    .build();
            logger.debug("Init GET query");
            HttpGet get = new HttpGet(uri);
            logger.debug("Init GET Client");
            CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslf).build();
            logger.debug("Try to get response");
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.warn("Meter's data are wrong!");
                return null;
            }
            //logger.info("Returning data " + EntityUtils.toString(response.getEntity()).toString());
            return new JSONObject(EntityUtils.toString(response.getEntity())).getJSONArray("body");
        } catch (URISyntaxException ex) {
            logger.error("Something went wrong! " + ex.getMessage());
        } catch (IOException | JSONException ex) {
            logger.error("Something went wrong! " + ex.getMessage());
        }
        return null;
    }

    private void sendMessage(String value, long timestamp, String hash) {
        if (value != null) {
            String topic = templateTopic.replace("${DEVICE_HASH}", hash);

            final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                    .format(new Date(timestamp));

            String message = ddi
                    .getTemplateObservation()
                    .replace("${DOMAIN}", ddi.getDomain())
                    .replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
                    .replace("${SENSOR_PATH}", ddi.getPathSensorUri())
                    .replace("${DEVICE_HASH}", hash)
                    .replace("${SENSOR_ID}", "1")
                    .replace("${TIMESTAMP}", String.valueOf(timestamp))
                    .replace("${DATETIME}", date)
                    .replace("${VALUE}", value);

            ddi.publish(topic, message);
        } else {
            logger.warn(hash + " has unknown value (null)");
        }
    }

    private String getHash(String id) {
        String name = id + ddi.getDriverName();
        int h = FNV_32_INIT;
        final int len = name.length();
        for (int i = 0; i < len; i++) {
            h ^= name.charAt(i);
            h *= FNV_32_PRIME;
        }
        long longHash = h & 0xffffffffl;
        return String.valueOf(longHash);
    }
}
