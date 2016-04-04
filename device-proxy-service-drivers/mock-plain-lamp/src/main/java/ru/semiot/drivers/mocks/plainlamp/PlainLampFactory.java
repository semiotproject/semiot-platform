package ru.semiot.drivers.mocks.plainlamp;

import org.apache.jena.rdf.model.Model;
import ru.semiot.platform.deviceproxyservice.api.drivers.Command;

public class PlainLampFactory {

    public Command parseCommand(Model command) {
        return new SwitchOperationCommand();
    }

}
