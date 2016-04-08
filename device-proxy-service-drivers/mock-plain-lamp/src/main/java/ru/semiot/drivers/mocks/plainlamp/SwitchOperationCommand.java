package ru.semiot.drivers.mocks.plainlamp;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Command;

import java.io.IOException;
import java.time.ZonedDateTime;

public class SwitchOperationCommand extends Command {

    private static final String TEMPLATE_PATH = "/ru/semiot/drivers/mocks/plainlamp/command.ttl";

    private static String DESCRIPTION_TEMPLATE;

    static {
        try {
            DESCRIPTION_TEMPLATE = IOUtils.toString(PlainLamp.class
                    .getResourceAsStream(TEMPLATE_PATH));
        } catch (IOException ex) {
            LoggerFactory.getLogger(PlainLamp.class)
                    .error(ex.getMessage(), ex);
        }
    }

    public SwitchOperationCommand(String deviceId, ZonedDateTime dateTime,
                                  Resource operationUri) {
        super(deviceId, dateTime);

        getProperties().put(PlainLampProperties.TARGET_OPERATION, operationUri.getURI());
    }

    @Override
    public String getRDFTemplate() {
        return DESCRIPTION_TEMPLATE;
    }
}
