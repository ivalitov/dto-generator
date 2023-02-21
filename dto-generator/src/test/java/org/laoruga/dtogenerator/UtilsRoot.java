package org.laoruga.dtogenerator;

import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.laoruga.dtogenerator.config.DtoGeneratorConfig;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.TypeGeneratorsConfig;
import org.laoruga.dtogenerator.rule.RulesInfoExtractor;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 05.02.2023
 */
public class UtilsRoot {

    public static RulesInfoExtractor getExtractorInstance(String... groups) {
        FieldFilter fieldsFilter;
        if (groups.length == 0) {
            fieldsFilter = new FieldFilter();
        } else {
            fieldsFilter = new FieldFilter();
            fieldsFilter.includeGroups(groups);
        }
        return new RulesInfoExtractor(fieldsFilter);
    }

    public static Map<String, Exception> getErrorsMap(DtoGenerator<?> dtoGenerator) {
        return dtoGenerator.getErrorsHolder().getErrors().entrySet().stream().collect(Collectors.toMap(
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
        Field configField = config.getClass().getSuperclass().getDeclaredField("generatorsConfig");
        configField.setAccessible(true);
        AtomicReference<TypeGeneratorsConfig> buildersConfig =
                (AtomicReference<TypeGeneratorsConfig>) configField.get(config);
        buildersConfig.set(new TypeGeneratorsConfig());
    }
}
