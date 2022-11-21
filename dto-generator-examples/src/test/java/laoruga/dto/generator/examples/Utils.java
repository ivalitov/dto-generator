package laoruga.dto.generator.examples;

import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
public class Utils {

    @SneakyThrows
    public static String toJson(Object object) {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
