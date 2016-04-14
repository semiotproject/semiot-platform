package ru.semiot.services.tsdbservice.model;

import com.datastax.driver.core.UDTValue;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.commons.namespaces.SEMIOT;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

public class Actuation {

    private static final String SENSOR_URI_PREFIX = CONFIG.sensorsURIPrefix() + "systems/";
    private final String systemId;
    private final ZonedDateTime eventTime;
    private final String type;
    private final List<CommandProperty> properties = new ArrayList<>();

    public Actuation(@NotNull String systemId, @NotNull String eventTime,
                     @NotNull String type) {
        this.systemId = systemId;
        this.eventTime = ZonedDateTime.parse(eventTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.type = type;
    }

    public void addProperty(Property property, RDFNode value) {
        if (value.isLiteral() || value.isURIResource()) {
            properties.add(new CommandProperty(property, value));
        }
    }

    public void addProperties(List<UDTValue> properties) {
        if (properties != null && !properties.isEmpty()) {
            for (UDTValue value : properties) {
                Property property = ResourceFactory.createProperty(value.getString("property"));
                RDFNode propertyValue;
                if (value.isNull("datatype")) {
                    propertyValue = ResourceFactory.createResource(value.getString("value"));
                } else {
                    propertyValue = ResourceFactory.createTypedLiteral(value.getString("value"),
                            TypeMapper.getInstance().getSafeTypeByName(value.getString("datatype")));
                }

                addProperty(property, propertyValue);
            }
        }
    }

    public String toInsertQuery() {
        if (properties.isEmpty()) {
            return String.format("INSERT INTO semiot.actuation " +
                    "(system_id, event_time, command_type) " +
                    "VALUES ('%s', '%s', '%s')", systemId, eventTime, type);
        } else {
            StringBuilder builder = new StringBuilder("[");
            for (CommandProperty p : properties) {
                builder.append(p.toCQLValue()).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append("]");

            return String.format(
                    "INSERT INTO semiot.actuation " +
                            "(system_id, event_time, command_properties, command_type)" +
                            "VALUES ('%s', '%s', %s, '%s')",
                    systemId, eventTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), builder.toString(), type);
        }
    }

    public Model toRDF() {
        Model model = ModelFactory.createDefaultModel();
        Resource actuation = ResourceFactory.createResource();
        Resource actuationValue = ResourceFactory.createResource();
        Resource system = ResourceFactory.createResource(SENSOR_URI_PREFIX + systemId);

        model.add(actuation, RDF.type, SEMIOT.Actuation)
                .add(actuation, SEMIOT.hasValue, actuationValue)
                .add(actuation, DUL.hasEventTime, eventTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .add(actuation, DUL.involvesAgent, system);

        model.add(actuationValue, RDF.type, SEMIOT.Command)
                .add(actuationValue, RDF.type, ResourceFactory.createResource(type))
                .add(actuationValue, DUL.involvesAgent, system);

        //TODO: for command parameters
        //for (Property property : properties) {
        //  Resource propertyResource = ResourceFactory.createResource();
        //  model.add(actuationValue, SEMIOT.hasProcessParameterValue, propertyResource)
        //      .add(propertyResource, SEMIOT.forProperty, property.property)
        //      .add(propertyResource, DUL.hasParameterDataValue, property.value);
        //}

        for (CommandProperty property : properties) {
            model.add(actuationValue, property.property, property.value);
        }

        return model;
    }


    private class CommandProperty {

        private final Property property;
        private final RDFNode value;

        CommandProperty(Property property, RDFNode value) {
            this.property = property;
            this.value = value;
        }

        public String toCQLValue() {
            if (value.isLiteral()) {
                Literal l = (Literal) value;
                return String.format("{property:'%s',value:'%s',datatype:'%s'}",
                        property.getURI(), l.getLexicalForm(), l.getDatatypeURI());
            } else {
                Resource r = (Resource) value;
                return String.format("{property:'%s',value:'%s',datatype:null}",
                        property.getURI(), r.getURI());
            }
        }
    }
}
