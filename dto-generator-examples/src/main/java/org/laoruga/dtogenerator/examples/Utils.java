package org.laoruga.dtogenerator.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
@Slf4j
public class Utils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    public static String toJson(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    @SneakyThrows
    public static void logJson(Object object) {
        log.info(toJson(object));
    }
}
