package ru.semiot.wamp;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

public class TopicListener implements Observer<String> {

    private static final Logger logger = LoggerFactory
            .getLogger(TopicListener.class);
    private final String topicName;

    public TopicListener(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(String message) {
        //Engine.writeFile(message);
        Model description = ModelFactory.createDefaultModel().read(
                new StringReader(message), null, SubscribeListener.LANG);
        if (!description.isEmpty()) {
            sendData(message);
        }
    }

    public static OutputStreamWriter init() {
        URLConnection conn;
        try {
            conn = new URL("http://localhost:8080/Web-1.0-SNAPSHOT/web/rest/data/").openConnection();
            conn.setDoOutput(true);
            return new OutputStreamWriter(conn.getOutputStream());
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException in method init()");
        } catch (IOException e) {
            logger.error("IOException in method init()");
        }
        return null;
    }

    private void sendData(String msg) {
        try {
            URLConnection conn;
            conn = new URL("http://localhost:8080/Web-1.0-SNAPSHOT/web/rest/data/").openConnection();
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            
            wr.write(msg);
            wr.flush();
            wr.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logger.debug(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            logger.error("MalformedURLException in method sendData()");
        } catch (IOException e) {
            logger.error("IOException in method sendData()");
        }
    }

}
