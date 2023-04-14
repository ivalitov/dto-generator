package org.laoruga.dtogenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.config.Configuration;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;
import org.laoruga.dtogenerator.rule.RulesInfoExtractor;

import java.lang.reflect.Field;

/**
 * @author Il'dar Valitov
 * Created on 05.02.2023
 */
@Slf4j
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
        Configuration configurationHolder = DtoGeneratorStaticConfig.getInstance();
        configurationHolder.getDtoGeneratorConfig().setGenerateAllKnownTypes(false);
        Field configField = configurationHolder.getClass().getDeclaredField("typeGeneratorsConfig");
        configField.setAccessible(true);
        configField.set(configurationHolder, new TypeGeneratorsConfigLazy());
        log.info("Static context restored");
    }
}
