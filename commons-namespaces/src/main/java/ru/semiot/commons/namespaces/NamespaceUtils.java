package ru.semiot.commons.namespaces;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class NamespaceUtils {
    
    private static final String PREFIX = "PREFIX";
    private static final String BLANK = " ";
    private static final String COLON = ":";
    private static final String LT = "<";
    private static final String GT = ">";
    private static final String NEWLINE = "\n";
    private static final String FIELD_URI = "URI";

    /**
     * Generates prologue for SPARQL queries.
     * 
     * {@code namespaces} must have a public static field named {@code uri} 
     * (ignoring case) which is {@code String} containing the uri of the namespace.
     * 
     * @param namespaces
     * @return 
     */
    public static String toSPARQLPrologue(Class... namespaces) {
        StringBuilder builder = new StringBuilder();
        
        for (Class<? extends Namespace> clazz : namespaces) {
            try {
                List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
                Field f_uri = fields.stream().filter((f) -> {
                    return f.getName().equalsIgnoreCase(FIELD_URI);
                }).findFirst().get();
                
                f_uri.setAccessible(true);
                if (f_uri.isAccessible() && !clazz.isAnonymousClass()) {
                    Object uri = f_uri.get(null);
                    String prefix = clazz.getSimpleName().toLowerCase();
                    
                    if (uri instanceof String) {
                        builder.append(PREFIX).append(BLANK).append(prefix)
                                .append(COLON).append(BLANK).append(LT)
                                .append(uri).append(GT).append(NEWLINE);
                        
                        continue;
                    }
                }
                
                throw new IllegalStateException();
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        return builder.toString();
    }

}
