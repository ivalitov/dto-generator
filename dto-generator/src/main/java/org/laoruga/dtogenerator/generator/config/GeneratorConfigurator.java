package org.laoruga.dtogenerator.generator.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.constants.Boundary.MIN_VALUE;
import static org.laoruga.dtogenerator.constants.Boundary.NULL_VALUE;
import static org.laoruga.dtogenerator.constants.RulesInstance.NUMBER_RULE_ZEROS;
import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorConfigurator {

    @Getter(AccessLevel.PUBLIC)
    private final ConfigurationHolder configuration;

    @Getter(AccessLevel.PUBLIC)
    RemarksHolder remarksHolder;

    public static final Consumer<ConfigDto> EMPTY_SPECIFIC_CONFIG = configDto -> {
        log.debug("Specific config is absent.");
    };

    protected GeneratorConfigurator(ConfigurationHolder configuration,
                                    RemarksHolder remarksHolder) {
        this.configuration = configuration;
        this.remarksHolder = remarksHolder;
    }

    public RuleRemark getRuleRemarkOrNull(String fieldName) {
        return remarksHolder.getBoundaryOrNull(fieldName);
    }

    protected ConfigDto mergeGeneratorConfigurations(Supplier<ConfigDto> newConfigInstanceSupplier,
                                                     Class<?> fieldType,
                                                     String fieldName) {
        return mergeGeneratorConfigurations(
                newConfigInstanceSupplier,
                EMPTY_SPECIFIC_CONFIG,
                fieldType,
                fieldName);
    }

    public ConfigDto mergeGeneratorConfigurations(Supplier<ConfigDto> newConfigInstanceSupplier,
                                                  Consumer<ConfigDto> specificConfiguration,
                                                  Class<?> fieldType,
                                                  String fieldName) {

        ConfigDto config = newConfigInstanceSupplier.get();

        ConfigDto staticConfig = ((TypeGeneratorsConfigLazy) DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig())
                .getOrNull(fieldType);

        ConfigDto instanceConfig = getConfiguration().getTypeGeneratorsConfig()
                .getOrNull(fieldType);

        ConfigDto fieldConfig = getConfiguration().getTypeGeneratorsConfigForField()
                .getOrNull(fieldName, config.getClass());

        if (staticConfig != null) {
            config.merge(staticConfig);
        }

        if (instanceConfig != null) {
            config.merge(instanceConfig);
        }

        if (fieldConfig != null) {
            config.merge(fieldConfig);
        }

        if (getRuleRemarkOrNull(fieldName) != null) {
            config.setRuleRemark(getRuleRemarkOrNull(fieldName));
        }

        if (specificConfiguration != null) {
            specificConfiguration.accept(config);
        }

        return config;
    }


    /*
     * Specific type configurations
     */

    @SuppressWarnings("unchecked")
    public static Consumer<ConfigDto> getEnumGeneratorSpecificConfig(Class<?> generatedType) {
        return (config) -> {
            EnumConfig enumConfig = (EnumConfig) config;
            if (enumConfig.getEnumClass() == null) {
                if (generatedType.isEnum()) {
                    enumConfig.setEnumClass((Class<? extends Enum<?>>) generatedType);
                } else {
                    throw new DtoGeneratorException("Field type must be enum : '"
                            + generatedType + "'");
                }
            }
        };
    }

    public static Consumer<ConfigDto> getTemporalGeneratorSpecificConfig(Class<? extends Temporal> generatedType) {
        return (config) -> {
            DateTimeConfig dateTimeConfig = (DateTimeConfig) config;
            dateTimeConfig.setGeneratedType(generatedType);
        };
    }

    public static Consumer<ConfigDto> getCollectionGeneratorSpecificConfig(Class<? extends Collection<?>> generatedType,
                                                                           Generator<?> elementGenerator) {
        return (config) -> {
            CollectionConfig collectionConfig = (CollectionConfig) config;
            if (collectionConfig.getCollectionInstanceSupplier() == null) {
                collectionConfig.setCollectionInstanceSupplier(
                        () -> createInstance(generatedType)
                );
            }
            if (collectionConfig.getElementGenerator() == null) {
                collectionConfig.setElementGenerator(elementGenerator);
            }
        };
    }

    public static Consumer<ConfigDto> getArrayGeneratorSpecificConfig(Class<?> elementType,
                                                                      Generator<?> elementGenerator) {
        return (config) -> {
            ArrayConfig arrayConfig = (ArrayConfig) config;
            arrayConfig.setElementType(elementType);
            if (arrayConfig.getElementGenerator() == null) {
                arrayConfig.setElementGenerator(elementGenerator);
            }
        };
    }


    @SuppressWarnings("unchecked")
    public static Consumer<ConfigDto> getMapGeneratorSpecificConfig(Class<? extends Map<?, ?>> generatedType,
                                                                    Generator<?> keyGenerator,
                                                                    Generator<?> valueGenerator) {
        return (config) -> {
            MapConfig mapConfig = (MapConfig) config;
            if (mapConfig.getMapInstanceSupplier() == null) {
                mapConfig.setMapInstanceSupplier(
                        () -> (Map<Object, Object>) createInstance(generatedType)
                );
            }
            if (mapConfig.getKeyGenerator() == null) {
                mapConfig.setKeyGenerator((Generator<Object>) keyGenerator);
            }
            if (mapConfig.getValueGenerator() == null) {
                mapConfig.setValueGenerator((Generator<Object>) valueGenerator);
            }
        };
    }


    @SuppressWarnings("unchecked")
    static Consumer<ConfigDto> integerGeneratorSpecificConfig(Class<?> fieldType,
                                                              String fieldName) {
        return (config) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                config.merge(new IntegralConfig(NUMBER_RULE_ZEROS, (Class<? extends Number>) fieldType)
                        .setRuleRemark(MIN_VALUE));
            }
        };
    }

    static Consumer<ConfigDto> decimalGeneratorSpecificConfig(Class<?> fieldType,
                                                              String fieldName) {
        return (config) -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                ((DecimalConfig) config)
                        .setMinValue(0D)
                        .setMaxValue(0D)
                        .setRuleRemark(MIN_VALUE);
            }
        };
    }

    static Consumer<ConfigDto> booleanGeneratorSpecificConfig(Class<?> fieldType,
                                                              String fieldName) {
        return config -> {
            if (config.getRuleRemark() == NULL_VALUE && fieldType.isPrimitive()) {
                reportPrimitiveCannotBeNull(fieldName);
                ((BooleanConfig) config).setTrueProbability(0D).setRuleRemark(MIN_VALUE);
            }
        };
    }

    protected static void reportPrimitiveCannotBeNull(String fieldName) {
        log.warn("Primitive field " + fieldName + " can't be null, it will be assigned to '0'");
    }

}
