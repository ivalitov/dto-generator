package org.laoruga.dtogenerator.functional.util;

import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.ErrorsHolder;
import org.laoruga.dtogenerator.config.DtoGeneratorConfig;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.TypeGeneratorBuildersConfig;

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

        Map<Field, Exception> errors;
        try {
            Field mapperField = dtoGenerator.getClass().getDeclaredField("errorsHolder");
            mapperField.setAccessible(true);
            ErrorsHolder errorsHolder = (ErrorsHolder) mapperField.get(dtoGenerator);
            Field errorsField = errorsHolder.getClass().getDeclaredField("errors");
            errorsField.setAccessible(true);
            errors = (Map<Field, Exception>) errorsField.get(errorsHolder);
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

    @SneakyThrows
    public static String toJson(Object object) {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static void resetStaticConfig() {
        DtoGeneratorConfig config = DtoGeneratorStaticConfig.getInstance();
        config.setGenerateAllKnownTypes(false);
        Field configField = config.getClass().getSuperclass().getDeclaredField("genBuildersConfig");
        configField.setAccessible(true);
        AtomicReference<TypeGeneratorBuildersConfig> buildersConfig =
                (AtomicReference<TypeGeneratorBuildersConfig>) configField.get(config);
        buildersConfig.set(new TypeGeneratorBuildersConfig());
    }
}
