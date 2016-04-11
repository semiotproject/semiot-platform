package ru.semiot.commons.restapi;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Provider
public class ZoneDateTimeProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
                                              Annotation[] annotations) {
        if (rawType.getName().equals(java.time.ZonedDateTime.class.getName())) {
            return new ParamConverter<T>() {
                @Override
                public T fromString(String value) {
                    if (value == null || value.isEmpty()) {
                        return null;
                    } else {
                        java.time.ZonedDateTime dateTime = java.time.ZonedDateTime.parse(
                                value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        return rawType.cast(dateTime);
                    }
                }

                @Override
                public String toString(T value) {
                    if (value instanceof ZonedDateTime) {
                        return ((ZonedDateTime) value).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }
                    return null;
                }
            };
        }
        return null;
    }
}
