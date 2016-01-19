package ru.semiot.platform.apigateway;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.impl.TurtleRDFParser;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;

public class JsonLdTests {

    @Test
    public void createSimpleJsonLdWithJsonlDBuilder() throws JsonLdError, IOException {
        Map<String, Object> CONTEXT = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

        JsonLdBuilder builder = new JsonLdBuilder()
                .context(CONTEXT)
                .append("@id", "http://example.com/a#")
                .append("@type", "hydra:ApiDocumentation")
                .append("hydra:entrypoint", "http://example.com/api");

        System.out.println(builder.toCompactedString());
    }

    @Test
    public void createSimpleJsonLdWithMap() throws JsonLdError, IOException {
        Map<String, Object> CONTEXT = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

        Map<String, Object> content = new HashMap<>();
        content.put("@id", "http://example.com/a#");
        content.put("@type", "ApiDocumentation");
        content.put("entrypoint", "http://example.com/api");

        System.out.println(JsonUtils.toString(JsonLdProcessor.compact(
                content, CONTEXT, new JsonLdOptions())));
    }

    @Test
    public void createSimpleJsonLdWithList() throws JsonLdError, IOException {
        Map<String, Object> CONTEXT = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

        JsonLdBuilder builder = new JsonLdBuilder()
                .context(CONTEXT)
                .append("@id", "http://example.com/a#")
                .append("@type", "hydra:ApiDocumentation")
                .append("hydra:entrypoint", "http://example.com/api")
                .append("hydra:member", new HashMap<>());

        System.out.println(builder.toCompactedString());
    }

    @Test
    public void testJsonLdFromRDF() throws JsonLdError, IOException {
        final String INPUT_RDF 
                = "_:B7b332b8bX3A1515d21b98dX3AX2D7f86 <http://purl.org/NET/ssnext/communication#hasCommunicationEndpoint> <coap://192.168.0.114:60003/meter/temperature/obs> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f86 <http://purl.oclc.org/NET/ssnx/ssn#hasMeasurementCapability> _:B7b332b8bX3A1515d21b98dX3AX2D7f85 .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f86 <http://purl.oclc.org/NET/ssnx/ssn#observes> <http://qudt.org/vocab/quantity#ThermodynamicTemperature> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f86 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.oclc.org/NET/ssnx/ssn#SensingDevice> .\n" +
"<http://localhost/api/systems/3691856330> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.oclc.org/NET/ssnx/ssn#System> .\n" +
"<http://localhost/api/systems/3691856330> <http://purl.org/dc/terms/#identifier> \"3691856330\"^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
"<http://localhost/api/systems/3691856330> <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> _:B7b332b8bX3A1515d21b98dX3AX2D7f86 .\n" +
"<http://localhost/api/systems/3691856330> <http://purl.oclc.org/NET/ssnx/ssn#hasSubSystem> _:B7b332b8bX3A1515d21b98dX3AX2D7f8a .\n" +
"<http://localhost/api/systems/3691856330> <http://ontology.tno.nl/saref#hasState> <http://ontology.tno.nl/saref#OffState> .\n" +
"<http://localhost/api/systems/3691856330> <http://purl.org/NET/ssnext/communication#hasCommunicationEndpoint> <ws://wamprouter/ws?topic=3691856330> .\n" +
"<http://localhost/api/systems/3691856330> <http://www.w3.org/2000/01/rdf-schema#label> \"Heat Meter #60003\" .\n" +
"<http://localhost/api/systems/3691856330> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/NET/ssnext/heatmeters#HeatMeter> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f89 <http://purl.oclc.org/NET/ssnx/ssn#hasMeasurementProperty> _:B7b332b8bX3A1515d21b98dX3AX2D7f8c .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f89 <http://purl.oclc.org/NET/ssnx/ssn#forProperty> <http://qudt.org/vocab/quantity#SpecificHeatCapacity> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f89 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.oclc.org/NET/ssnx/ssn#MeasurementCapability> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f8c <http://purl.oclc.org/NET/ssnx/ssn#hasValue> _:B7b332b8bX3A1515d21b98dX3AX2D7f8b .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f8c <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.qudt.org/qudt/owl/1.0.0/qudt/#Unit> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f87 <http://purl.oclc.org/NET/ssnx/ssn#hasValue> <http://qudt.org/vocab/unit#DegreeCelsius> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f87 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.qudt.org/qudt/owl/1.0.0/qudt/#Quantity> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f8a <http://purl.org/NET/ssnext/communication#hasCommunicationEndpoint> <coap://192.168.0.114:60003/meter/heat/obs> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f8a <http://purl.oclc.org/NET/ssnx/ssn#hasMeasurementCapability> _:B7b332b8bX3A1515d21b98dX3AX2D7f89 .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f8a <http://purl.oclc.org/NET/ssnx/ssn#observes> <http://qudt.org/vocab/quantity#SpecificHeatCapacity> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f8a <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.oclc.org/NET/ssnx/ssn#SensingDevice> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f85 <http://purl.oclc.org/NET/ssnx/ssn#hasMeasurementProperty> _:B7b332b8bX3A1515d21b98dX3AX2D7f88 .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f85 <http://purl.oclc.org/NET/ssnx/ssn#forProperty> <http://qudt.org/vocab/quantity#ThermodynamicTemperature> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f85 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.oclc.org/NET/ssnx/ssn#MeasurementCapability> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f88 <http://purl.oclc.org/NET/ssnx/ssn#hasValue> _:B7b332b8bX3A1515d21b98dX3AX2D7f87 .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f88 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.qudt.org/qudt/owl/1.0.0/qudt/#Unit> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f8b <http://purl.oclc.org/NET/ssnx/ssn#hasValue> <http://qudt.org/vocab/unit#Kilocalorie> .\n" +
"_:B7b332b8bX3A1515d21b98dX3AX2D7f8b <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.qudt.org/qudt/owl/1.0.0/qudt/#Quantity> .";

        Object object = JsonLdProcessor
                .fromRDF(INPUT_RDF, new TurtleRDFParser());
        
        System.out.println(JsonUtils.toString(object));
    }
}
