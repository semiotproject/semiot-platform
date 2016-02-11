package ru.semiot.platform.apigateway;

import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JsonLdContextProviderServiceTest {

    @Inject
    ContextProvider service;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(ContextProvider.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void test() throws IOException, URISyntaxException {
        System.out.println(JsonUtils.toPrettyString(service.getRDFModel(
                ContextProvider.API_DOCUMENTATION, 
                new URI("http://demo.semiot.ru"))));
    }

}
