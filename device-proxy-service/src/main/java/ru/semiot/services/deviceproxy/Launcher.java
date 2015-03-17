package ru.semiot.services.deviceproxy;

import org.aeonbits.owner.ConfigFactory;
import org.msgpack.core.example.MessagePackExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger logger
            = LoggerFactory.getLogger(Launcher.class);
    private static final ServiceConfig config = 
            ConfigFactory.create(ServiceConfig.class);

    public static final void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.run();
    }

    private void run() {
        try (CoAPInterface coap = new CoAPInterface()) {
            coap.start();
            
            WAMPClient.getInstance().init();
            
            synchronized (this) {
                while (!Thread.interrupted()) {
                    logger.info("Press Ctrl+C to stop");
                    wait();
                }
            }
        } catch (Exception ex) {
            logger.info(ex.getMessage(), ex);
        } finally {
            
        }
    }

}
