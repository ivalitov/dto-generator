package org.laoruga.dtogenerator.config.types;

import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.api.rules.DecimalRule;
import org.laoruga.dtogenerator.api.rules.EnumRule;
import org.laoruga.dtogenerator.api.rules.NumberRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;

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

    public StringConfigDto getStringConfig() {
        return (StringConfigDto) getConfigLazy(
                StringRule.GENERATED_TYPES,
                StringConfigDto::new
        );
    }

    public NumberCommonConfigDto getNumberConfig() {
        return (NumberCommonConfigDto) getConfigLazy(
                NumberRule.GENERATED_TYPES,
                NumberCommonConfigDto::new
        );
    }

    public DecimalCommonConfigDto getDecimalConfig() {
        return (DecimalCommonConfigDto) getConfigLazy(
                DecimalRule.GENERATED_TYPES,
                DecimalCommonConfigDto::new
        );
    }

    public DateTimeConfigDto getDateTimeConfig(Class<? extends Temporal> dateTimeType) {
        return (DateTimeConfigDto) getConfigLazy(
                dateTimeType,
                DateTimeRule.GENERATED_TYPES,
                DateTimeConfigDto::new
        );
    }

    public EnumConfigDto getEnumConfig() {
        return (EnumConfigDto) getConfigLazy(
                EnumRule.GENERATED_TYPES,
                EnumConfigDto::new
        );
    }

    public CollectionConfigDto getCollectionConfig(Class<? extends Collection> generatedType) {
        return (CollectionConfigDto) getConfigLazy(
                new Class[]{generatedType},
                CollectionConfigDto::new
        );
    }

    private ConfigDto getConfigLazy(Class<?>[] generatedTypes,
                                    Supplier<ConfigDto> configSupplier) {
        initConfig(generatedTypes, true, configSupplier);
        return Objects.requireNonNull(configMap.get(generatedTypes[0]));
    }

    private ConfigDto getConfigLazy(Class<?> dateTimeType,
                                    Class<?>[] generatedTypes,
                                    Supplier<ConfigDto> configSupplier) {
        if (Arrays.stream(generatedTypes).noneMatch(it -> it == dateTimeType)) {
            throw new DtoGeneratorException("Unknown generated type: '" + dateTimeType + "'");
        }
        if (!configMap.containsKey(dateTimeType)) {
            configMap.put(dateTimeType, configSupplier.get());
        }
        return configMap.get(dateTimeType);
    }

    private void initConfig(Class<?>[] generatedTypes, boolean sameConfigInstance, Supplier<ConfigDto> configSupplier) {
        ConfigDto configDto = null;

        for (Class<?> generatedType : generatedTypes) {
            if (!configMap.containsKey(generatedType)) {
                if (!sameConfigInstance || configDto == null) {
                    configDto = configSupplier.get();
                }
                configMap.putIfAbsent(generatedType, configDto);
            }
        }
    }

    public void setGeneratorConfigForType(Class<?> generatedType, ConfigDto generatorConfig) {
        if (configMap.containsKey(generatedType)) {
            throw new DtoGeneratorException("Generator config '" + generatorConfig.getClass() +
                    "' already has been set explicitly for type: '" + generatedType + "'");
        }
        configMap.put(generatedType, generatorConfig);
    }

}
