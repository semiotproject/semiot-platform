package ru.semiot.commons.namespaces;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class NamespaceUtils {
    
    private static final String PREFIX = "PREFIX";
    private static final String BLANK = " ";
    private static final String COLON = ":";
    private static final String LT = "<";
    private static final String GT = ">";
    private static final String NEWLINE = "\n";
    private static final String FIELD_PREFIX = "PREFIX";
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
                
                Optional<Field> f_prefix_opt = fields.stream().filter((f) -> {
                    return f.getName().equalsIgnoreCase(FIELD_PREFIX);
                }).findFirst();
                
                String prefix;
                if(f_prefix_opt.isPresent()) {
                    Field f_prefix = f_prefix_opt.get();
                    f_prefix.setAccessible(true);
                    prefix = (String) f_prefix.get(null);
                } else {
                    prefix = clazz.getSimpleName().toLowerCase();
                }
                
                f_uri.setAccessible(true);
                if (f_uri.isAccessible() && !clazz.isAnonymousClass()) {
                    Object uri = f_uri.get(null);
                    
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
