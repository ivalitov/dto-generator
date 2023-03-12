package org.laoruga.dtogenerator.config.types;

import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.*;
import org.laoruga.dtogenerator.generator.configs.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Provides lazy getters of config instances containing default configuration (default values from annotations).
 *
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */


public class TypeGeneratorsConfigLazy implements TypeGeneratorsConfigSupplier {

    @Getter(AccessLevel.PACKAGE)
    private final Map<Class<?>, ConfigDto> configMap = new HashMap<>();

    /**
     * @param generatedType field type for which the value is supposed to be generated
     * @return config instance or null if there is no config found according to the given classes
     */
    public ConfigDto getOrNull(Class<?> generatedType) {

        generatedType = Primitives.wrap(generatedType);

        if (configMap.containsKey(generatedType)) {
            return configMap.get(generatedType);
        }

        for (Map.Entry<Class<?>, ConfigDto> typeSupplierEntry : configMap.entrySet()) {

            if (typeSupplierEntry.getKey().isAssignableFrom(generatedType)) {
                return typeSupplierEntry.getValue();
            }
        }

        return null;
    }

    /*
     * Lazy getters
     */

    public StringConfigDto getStringConfig() {
        return (StringConfigDto) getConfigLazy(
                StringGeneratorBuilder.class,
                StringRule.GENERATED_TYPES,
                StringConfigDto::new
        );
    }

    public NumberCommonConfigDto getNumberConfig() {
        return (NumberCommonConfigDto) getConfigLazy(
                NumberGeneratorBuilder.class,
                NumberRule.GENERATED_TYPES,
                NumberCommonConfigDto::new
        );
    }

    public DecimalConfigDto getDoubleConfig() {
        return (DecimalConfigDto) getConfigLazy(
                DoubleGeneratorBuilder.class,
                DecimalRule.GENERATED_TYPES,
                DecimalConfigDto::new
        );
    }

    public LocalDateTimeConfigDto getLocalDateTimeConfig() {
        return (LocalDateTimeConfigDto) getConfigLazy(
                LocalDateTimeGeneratorBuilder.class,
                LocalDateTimeRule.GENERATED_TYPES,
                LocalDateTimeConfigDto::new
        );
    }

    public EnumConfigDto getEnumConfig() {
        return (EnumConfigDto) getConfigLazy(
                EnumGeneratorBuilder.class,
                EnumRule.GENERATED_TYPES,
                EnumConfigDto::new
        );
    }

    public CollectionConfigDto getCollectionConfig(Class<? extends Collection> generatedType) {
        return (CollectionConfigDto) getConfigLazy(
                CollectionGeneratorBuilder.class,
                new Class[]{generatedType},
                CollectionConfigDto::new
        );
    }

    private ConfigDto getConfigLazy(Class<? extends IGeneratorBuilder<?>> genBuilderClass,
                                    Class<?>[] generatedTypes,
                                    Supplier<ConfigDto> configSupplier) {
        ConfigDto configDto = null;
        for (Class<?> generatedType : generatedTypes) {
            if (!configMap.containsKey(generatedType)) {
                if (configDto == null) {
                    configDto = configSupplier.get();
                }
                configMap.putIfAbsent(generatedType, configDto);
            }
        }
        return configMap.putIfAbsent(generatedTypes[0], configDto);
    }

    public void setGeneratorConfigForType(Class<?> generatedType, ConfigDto generatorConfig) {
        if (configMap.containsKey(generatedType)) {
            throw new DtoGeneratorException("Generator config '" + generatorConfig.getClass() +
                    "' already has been set explicitly for type: '" + generatedType + "'");
        }
        configMap.put(generatedType, generatorConfig);
    }

}
