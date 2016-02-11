package ru.semiot.platform.apigateway.rest;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.net.URI;
import ru.semiot.platform.apigateway.utils.URIUtils;

public class ResourceUtils {

    public static final Resource createResourceFromClass(URI root, String className) {
        StringBuilder builder = new StringBuilder(URIUtils.extractRootURL(root))
                .append("/doc#")
                .append(className)
                .append("Resource");

        return ResourceFactory.createResource(builder.toString());
    }

}
