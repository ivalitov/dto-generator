package org.laoruga.dtogenerator.config;

import com.google.common.collect.ImmutableMap;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.*;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.rule.RulesInstance;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 20.02.2023
 */
public class MappingHelper {

    public static final Map<Class<?>, Class<?>> CONFIG_TYPE_TO_GENERATED_TYPE;
    public static final Map<Class<?>, Class<? extends IGeneratorBuilder>> CONFIG_TYPE_TO_BUILDER_TYPE;
    public static final Map<Class<?>, Supplier<ConfigDto>> GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER;

    static {
        Map<Class<?>, Class<?>> configToGeneratedType = new HashMap<>(16);
        Map<Class<?>, Class<? extends IGeneratorBuilder>> configToBuilder = new HashMap<>(16);
        Map<Class<?>, Supplier<ConfigDto>> configSupplier = new HashMap<>(16);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.booleanRule,
                BooleanConfigDto.class,
                BooleanGeneratorBuilder.class);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.stringRule,
                StringConfigDto.class,
                StringGeneratorBuilder.class);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.integerRule,
                IntegerConfigDto.class,
                IntegerGeneratorBuilder.class);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.longRule,
                LongConfigDto.class,
                LongGeneratorBuilder.class);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.doubleRule,
                DoubleConfigDto.class,
                DoubleGeneratorBuilder.class);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.localDateTimeRule,
                LocalDateTimeConfigDto.class,
                LocalDateTimeGeneratorBuilder.class);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.enumRule,
                EnumConfigDto.class,
                EnumGeneratorBuilder.class);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.listRule,
                CollectionConfigDto.class,
                CollectionGeneratorBuilder.class);

        add(configToGeneratedType, configToBuilder, configSupplier,
                RulesInstance.setRule,
                CollectionConfigDto.class,
                CollectionGeneratorBuilder.class);

        CONFIG_TYPE_TO_GENERATED_TYPE = ImmutableMap.copyOf(configToGeneratedType);
        CONFIG_TYPE_TO_BUILDER_TYPE = ImmutableMap.copyOf(configToBuilder);
        GENERATED_TYPE_TO_DEFAULT_CONFIG_NEW_INSTANCE_SUPPLIER = ImmutableMap.copyOf(configSupplier);
    }

    private static void add(
            Map<Class<?>, Class<?>> configToGeneratedType,
            Map<Class<?>, Class<? extends IGeneratorBuilder>> configToBuilder,
            Map<Class<?>, Supplier<ConfigDto>> generatedTypeToConfigSupplier,
            Annotation rule,
            Class<? extends ConfigDto> configClass,
            Class<? extends IGeneratorBuilder> builderClass
    ) {
        Class<?> generatedType;
        try {
            generatedType = ReflectionUtils.getDefaultMethodValue(
                    rule.annotationType(), "generatedType", Class.class);
        } catch (Exception e) {
            throw new DtoGeneratorException("Unexpected error while getting 'generatedType' from annotation class", e);
        }

        configToGeneratedType.put(configClass, generatedType);
        configToBuilder.put(configClass, builderClass);
        generatedTypeToConfigSupplier.put(generatedType, () -> ReflectionUtils.createInstance(configClass, rule));
    }
}
