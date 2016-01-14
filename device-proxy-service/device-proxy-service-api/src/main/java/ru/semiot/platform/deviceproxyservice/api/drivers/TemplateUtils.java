package ru.semiot.platform.deviceproxyservice.api.drivers;

import java.util.Map;

public abstract class TemplateUtils {

    private static final String VAR_LEFT = "${";
    private static final String VAR_RIGTH = "}";

    private static String wrap(String key) {
        return VAR_LEFT + key + VAR_RIGTH;
    }

    public static final String resolve(String template,
            Map<String, String>... maps) {

        String result = new String(template);

        for (Map<String, String> variables : maps) {

            for (String key : variables.keySet()) {
                result = result.replace(wrap(key), variables.get(key));
            }
        }

        return result;
    }
}
