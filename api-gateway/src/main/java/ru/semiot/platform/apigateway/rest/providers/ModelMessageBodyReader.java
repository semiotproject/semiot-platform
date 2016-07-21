package ru.semiot.platform.apigateway.rest.providers;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@Provider
public class ModelMessageBodyReader implements MessageBodyReader<Model> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType) {
    Lang lang = RDFLanguages.contentTypeToLang(mediaType.toString());
    if (lang == null) {
      return false;
    }
    return RDFLanguages.getRegisteredLanguages().contains(lang);
  }

  @Override
  public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                        InputStream entityStream)
      throws IOException, WebApplicationException {
    Lang lang = RDFLanguages.contentTypeToLang(mediaType.toString());

    return ModelFactory.createDefaultModel().read(entityStream, null, lang.getName());
  }
}
