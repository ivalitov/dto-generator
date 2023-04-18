package org.laoruga.dtogenerator.config.types;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.constants.RulesInstance;
import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@Slf4j
public final class TypeGeneratorsDefaultConfigSupplier {

    private static final Map<Class<?>, Supplier<ConfigDto>> GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER;

    static {
        Map<Class<?>, Supplier<ConfigDto>> configSupplier = new HashMap<>(10);

        add(configSupplier,
                BooleanRule.GENERATED_TYPE,
                RulesInstance.BOOLEAN_RULE,
                BooleanConfig.class,
                false);

        add(configSupplier,
                new Class[]{StringRule.GENERATED_TYPE},
                RulesInstance.STRING_RULE,
                StringConfig.class,
                false);

        add(configSupplier,
                IntegerRule.GENERATED_TYPES,
                RulesInstance.NUMBER_RULE,
                IntegerConfig.class,
                true);

        add(configSupplier,
                DecimalRule.GENERATED_TYPES,
                RulesInstance.DECIMAL_RULE,
                DecimalConfig.class,
                true);

        add(configSupplier,
                DateTimeRule.GENERATED_TYPE,
                RulesInstance.DATE_TIME_RULE,
                DateTimeConfig.class,
                false);

        add(configSupplier,
                EnumRule.GENERATED_TYPE,
                RulesInstance.ENUM_RULE,
                EnumConfig.class,
                false);

        add(configSupplier,
                CollectionRule.GENERATED_TYPE,
                RulesInstance.COLLECTION_RULE,
                CollectionConfig.class,
                false);

        add(configSupplier,
                MapRule.GENERATED_TYPE,
                RulesInstance.MAP_RULE,
                MapConfig.class,
                false);

        add(configSupplier,
                ArrayRule.GENERATED_TYPES,
                RulesInstance.ARRAY_RULE,
                ArrayConfig.class,
                true);

        GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER = ImmutableMap.copyOf(configSupplier);
    }

    public static Optional<Supplier<ConfigDto>> getDefaultConfigSupplier(Class<?> generatedType) {

        if (!GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.containsKey(generatedType)) {

            for (Map.Entry<Class<?>, Supplier<ConfigDto>> typeSupplierEntry :
                    GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.entrySet()) {

                if (typeSupplierEntry.getKey().isAssignableFrom(generatedType)) {
                    return Optional.of(typeSupplierEntry.getValue());
                }

            }

            log.info("Unable to get default config. Unknown type: " + generatedType);
            return Optional.empty();
        }

        return Optional.of(GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.get(generatedType));
    }

    private static void add(
            Map<Class<?>, Supplier<ConfigDto>> generatedTypeToConfigSupplier,
            Class<?>[] generatedTypes,
            Annotation rule,
            Class<? extends ConfigDto> configClass,
            boolean typeAsArgument
    ) {
        for (Class<?> generatedType : generatedTypes) {
            add(generatedTypeToConfigSupplier,
                    generatedType,
                    rule,
                    configClass,
                    typeAsArgument);
        }
    }

    private static void add(Map<Class<?>, Supplier<ConfigDto>> generatedTypeToConfigSupplier,
                            Class<?> generatedType,
                            Annotation rule,
                            Class<? extends ConfigDto> configClass,
                            boolean typeAsArgument) {
        if (typeAsArgument) {
            generatedTypeToConfigSupplier.put(generatedType,
                    () -> ReflectionUtils.createInstance(configClass, rule, generatedType));
        } else {
            generatedTypeToConfigSupplier.put(generatedType,
                    () -> ReflectionUtils.createInstance(configClass, rule));
        }
    }

}