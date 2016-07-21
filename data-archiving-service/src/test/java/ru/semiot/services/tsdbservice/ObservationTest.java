package ru.semiot.services.tsdbservice;


import static org.junit.Assert.assertTrue;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFLanguages;
import org.junit.Test;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.services.tsdbservice.model.Observation;

public class ObservationTest {

  @Test
  public void testToRDF1() {
    Observation observation = new Observation("123123", "123123-humidity",
        "2016-03-31T16:39:57+01:00",
        "http://qudt.org/vocab/quantity#ThermodynamicTemperature",
        null, "3.4");

    Model model = observation.toRDF();

    model.write(System.out, RDFLanguages.strLangTurtle);

    assertTrue(model.contains(
        null,
        SSN.observationResultTime,
        ResourceFactory.createTypedLiteral("2016-03-31T16:39:57+01:00",
            XSDDatatype.XSDdateTime)));
  }

  @Test
  public void testToRDF2() {
    Observation observation = new Observation("123123", "123123-humidity",
        "2016-03-31T16:39:57+01:00",
        "http://qudt.org/vocab/quantity#ThermodynamicTemperature",
        null, "3.4");

    Model model = observation.toRDF();

    model.write(System.out, RDFLanguages.strLangTurtle);

    assertTrue(model.contains(
        null,
        SSN.observationResultTime,
        ResourceFactory.createTypedLiteral("2016-03-31T16:39:57+01:00",
            XSDDatatype.XSDdateTime)));
  }
}
