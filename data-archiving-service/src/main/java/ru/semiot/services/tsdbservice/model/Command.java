package ru.semiot.services.tsdbservice.model;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Command {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ");
    private final String systemId;
    private final ZonedDateTime eventTime;
    private final String type;
    private final List<Property> properties = new ArrayList<>();

    public Command(@NotNull String systemId, @NotNull String eventTime,
                   @NotNull String type) {
        this.systemId = systemId;
        ZonedDateTime tmp;
        try {
            tmp = ZonedDateTime.parse(eventTime,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException dtpe) {
            //Must be the time came from Cassandra. Try it.
            tmp = ZonedDateTime.parse(eventTime, DTF);
        }
        this.eventTime = tmp;
        this.type = type;
    }

    public void addProperty(Resource property, RDFNode value) {
        if (value.isLiteral() || value.isURIResource()) {
            properties.add(new Property(property, value));
        }
    }

    public String toInsertCommandQuery() {
        if (properties.isEmpty()) {
            return String.format("INSERT INTO semiot.command " +
                    "(system_id, event_time, command_type) " +
                    "VALUES ('%s', '%s', '%s')", systemId, eventTime, type);
        } else {
            StringBuilder builder = new StringBuilder("[");
            for (Property p : properties) {
                builder.append(p.toCQLValue()).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append("]");

            return String.format(
                    "INSERT INTO semiot.command " +
                            "(system_id, event_time, command_properties, command_type)" +
                            "VALUES ('%s', '%s', %s, '%s')",
                    systemId, eventTime.format(DTF), builder.toString(), type);
        }
    }

    private class Property {

        private final Resource property;
        private final RDFNode value;

        Property(Resource property, RDFNode value) {
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
