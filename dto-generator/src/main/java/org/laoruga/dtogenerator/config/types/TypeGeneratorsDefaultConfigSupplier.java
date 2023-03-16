package org.laoruga.dtogenerator.config.types;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.constants.RulesInstance;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
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
                BooleanConfigDto.class,
                false);

        add(configSupplier,
                new Class[]{StringRule.GENERATED_TYPE},
                RulesInstance.STRING_RULE,
                StringConfigDto.class,
                false);

        add(configSupplier,
                NumberRule.GENERATED_TYPES,
                RulesInstance.NUMBER_RULE,
                NumberConfigDto.class,
                true);

        add(configSupplier,
                DecimalRule.GENERATED_TYPES,
                RulesInstance.DECIMAL_RULE,
                DecimalConfigDto.class,
                true);

        add(configSupplier,
                DateTimeRule.GENERATED_TYPE,
                RulesInstance.DATE_TIME_RULE,
                DateTimeConfigDto.class,
                false);

        add(configSupplier,
                EnumRule.GENERATED_TYPE,
                RulesInstance.ENUM_RULE,
                EnumConfigDto.class,
                false);

        add(configSupplier,
                CollectionRule.GENERATED_TYPE,
                RulesInstance.COLLECTION_RULE,
                CollectionConfigDto.class,
                false);

        GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER = ImmutableMap.copyOf(configSupplier);
    }

    public static Supplier<ConfigDto> getDefaultConfigSupplier(Class<?> generatedType) {

        if (!GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.containsKey(generatedType)) {

            for (Map.Entry<Class<?>, Supplier<ConfigDto>> typeSupplierEntry :
                    GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.entrySet()) {

                if (typeSupplierEntry.getKey().isAssignableFrom(generatedType)) {
                    return typeSupplierEntry.getValue();
                }

            }

            throw new IllegalArgumentException("Unknown type: " + generatedType);
        }

        return GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER.get(generatedType);
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