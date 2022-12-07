package org.laoruga.dtogenerator.functional.util;

import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.ErrorsMapper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 28.05.2022
 */
public class TestUtils {

    public static Map<String, Exception> getErrorsMap(DtoGenerator dtoGenerator) {

        AtomicReference<Map<Field, Exception>> errors;
        try {
            Field mapperField = dtoGenerator.getClass().getDeclaredField("errorsMapper");
            mapperField.setAccessible(true);
            ErrorsMapper errorsMapper = (ErrorsMapper) mapperField.get(dtoGenerator);
            Field errorsField = errorsMapper.getClass().getDeclaredField("errors");
            errorsField.setAccessible(true);
            errors = (AtomicReference<Map<Field, Exception>>) errorsField.get(errorsMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return errors.get().entrySet().stream().collect(Collectors.toMap(
                (e) -> e.getKey().getName(),
                Map.Entry::getValue
        ));
    }

    @SneakyThrows
    public static Field getField(Class<?> clazz, String fieldName) {
        return clazz.getDeclaredField(fieldName);
    }

    @SneakyThrows
    public static String toJson(Object object) {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
