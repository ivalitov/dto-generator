package org.laoruga.dtogenerator.functional.util;

import lombok.SneakyThrows;
import org.laoruga.dtogenerator.DtoGenerator;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 28.05.2022
 */
public class TestUtils {

    public static Map<String, Exception> getErrorsMap(DtoGenerator dtoGenerator) {
        Field errorsField = null;
        Map<Field, Exception> errors;
        try {
            errorsField = dtoGenerator.getClass().getDeclaredField("errors");
            errorsField.setAccessible(true);
            errors = (Map<Field, Exception>) errorsField.get(dtoGenerator);
            errorsField.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return errors.entrySet().stream().collect(Collectors.toMap(
                (e) -> e.getKey().getName(),
                Map.Entry::getValue
        ));
    }

    @SneakyThrows
    public static Field getField(Class<?> clazz, String fieldName) {
        return clazz.getDeclaredField(fieldName);
    }
}
