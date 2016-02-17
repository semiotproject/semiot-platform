
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;
import static org.junit.Assert.*;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SHACL;
import ru.semiot.commons.namespaces.SSN;

public class NamespaceUtilsTest {

    @Test
    public void testToSPARQLHeader() {
        String actual = NamespaceUtils.toSPARQLPrologue(Hydra.class, SSN.class, SHACL.class);

        String expected = "PREFIX hydra: <http://www.w3.org/ns/hydra/core#>\n"
                + "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n"
                + "PREFIX sh: <http://www.w3.org/ns/shacl#>\n";

        assertEquals(expected, actual);
    }
    
    @Test
    public void testToSPARQLHeaderWithJenaNamespaces() {
        String actual = NamespaceUtils.toSPARQLPrologue(Hydra.class, RDF.class);

        String expected = "PREFIX hydra: <http://www.w3.org/ns/hydra/core#>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";

        assertEquals(expected, actual);
    }

}
