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
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfigDto;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
                StringRule.GENERATED_TYPE,
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

    public EnumConfigDto getEnumConfig() {
        return (EnumConfigDto) getConfigLazy(
                EnumRule.GENERATED_TYPE,
                EnumConfigDto::new
        );
    }

    public DateTimeConfigDto getDateTimeConfig(Class<? extends Temporal> dateTimeType) {
        return (DateTimeConfigDto) getConfigLazy(
                dateTimeType,
                DateTimeConfigDto::new
        );
    }

    public CollectionConfigDto getCollectionConfig(Class<? extends Collection> generatedType) {
        return (CollectionConfigDto) getConfigLazy(
                generatedType,
                CollectionConfigDto::new
        );
    }

    public ArrayConfigDto getArrayConfig(Class<?> arrayType) {
        return (ArrayConfigDto) getConfigLazy(
                arrayType,
                ArrayConfigDto::new
        );
    }

    @Override
    public MapConfigDto getMapConfig(Class<? extends Map> generatedType) {
        return (MapConfigDto) getConfigLazy(
                generatedType,
                MapConfigDto::new
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
