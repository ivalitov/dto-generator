package org.laoruga.dtogenerator.config.types;

import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.api.rules.DecimalRule;
import org.laoruga.dtogenerator.api.rules.EnumRule;
import org.laoruga.dtogenerator.api.rules.NumberRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;

import java.time.temporal.Temporal;
import java.util.*;
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

    public StringConfig getStringConfig() {
        return (StringConfig) getConfigLazy(
                StringRule.GENERATED_TYPE,
                StringConfig::new
        );
    }

    public NumberCommonConfig getNumberConfig() {
        return (NumberCommonConfig) getConfigLazy(
                NumberRule.GENERATED_TYPES,
                NumberCommonConfig::new
        );
    }

    public DecimalCommonConfig getDecimalConfig() {
        return (DecimalCommonConfig) getConfigLazy(
                DecimalRule.GENERATED_TYPES,
                DecimalCommonConfig::new
        );
    }

    public EnumConfig getEnumConfig() {
        return (EnumConfig) getConfigLazy(
                EnumRule.GENERATED_TYPE,
                EnumConfig::new
        );
    }

    public DateTimeConfig getDateTimeConfig(Class<? extends Temporal> dateTimeType) {
        return (DateTimeConfig) getConfigLazy(
                dateTimeType,
                DateTimeConfig::new
        );
    }

    /**
     * Configuration apples to:
     * <li>provided class or interface</li>
     * <li>classes and interfaces that extends or implements provided type</li>
     * provided type.
     *<p/>
     * For example, config set for {@link List} will apply to {@link LinkedList}, {@link ArrayList} etc.
     *
     * @param generatedType class or interface to configure
     * @return configuration instantiated lazy
     */
    public CollectionConfig getCollectionConfig(Class<? extends Collection> generatedType) {
        return (CollectionConfig) getConfigLazy(
                generatedType,
                CollectionConfig::new
        );
    }

    /**
     * Configuration apples to provided array class.
     *
     * @param generatedType class or interface to configure
     *                      for example: int[].class, Integer[].class
     *                      (these are two different types which processed independently)
     * @return configuration instantiated lazy
     */
    public ArrayConfig getArrayConfig(Class<?> generatedType) {
        return (ArrayConfig) getConfigLazy(
                generatedType,
                ArrayConfig::new
        );
    }

    /**
     * Configuration apples to:
     * <li>provided class or interface</li>
     * <li>classes and interfaces that extends or implements provided type provided type</li>
     * provided type.
     *
     * @param generatedType class or interface to configure
     * @return configuration instantiated lazy
     */
    @Override
    public MapConfig getMapConfig(Class<? extends Map> generatedType) {
        return (MapConfig) getConfigLazy(
                generatedType,
                MapConfig::new
        );
    }

    private ConfigDto getConfigLazy(Class<?> generatedType,
                                    Supplier<ConfigDto> configSupplier) {
        if (!configMap.containsKey(generatedType)) {
            configMap.put(generatedType, configSupplier.get());
        }

        return configMap.get(generatedType);
    }

    private ConfigDto getConfigLazy(Class<?>[] generatedTypes,
                                    Supplier<ConfigDto> configSupplier) {
        ConfigDto configDto = configSupplier.get();

        for (Class<?> generatedType : generatedTypes) {
            if (!configMap.containsKey(generatedType)) {
                configMap.putIfAbsent(generatedType, configDto);
            }
        }

        return Objects.requireNonNull(configMap.get(generatedTypes[0]));
    }

    public void setGeneratorConfigForType(Class<?> generatedType, ConfigDto generatorConfig) {
        if (configMap.containsKey(generatedType)) {
            throw new DtoGeneratorException("Generator config '" + generatorConfig.getClass() +
                    "' already has been set explicitly for type: '" + generatedType + "'");
        }
        configMap.put(generatedType, generatorConfig);
    }

}
