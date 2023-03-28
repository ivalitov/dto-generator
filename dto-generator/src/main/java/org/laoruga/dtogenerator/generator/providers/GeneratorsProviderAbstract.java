package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public abstract class GeneratorsProviderAbstract {

    @Getter(AccessLevel.PROTECTED)
    private final ConfigurationHolder configuration;
    @Getter(AccessLevel.PROTECTED)
    private final RemarksHolder remarksHolder;

    protected static final Consumer<ConfigDto> EMPTY_SPECIFIC_CONFIG = configDto -> {
        log.debug("Specific config is absent.");
    };

    protected GeneratorsProviderAbstract(ConfigurationHolder configuration, RemarksHolder remarksHolder) {
        this.configuration = configuration;
        this.remarksHolder = remarksHolder;
    }

    public IRuleRemark getRuleRemark(String fieldName) {
        return remarksHolder.getBasicRemarks().isBasicRuleRemarkExists(fieldName) ?
                remarksHolder.getBasicRemarks().getBasicRuleRemark(fieldName) : null;
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

    protected ConfigDto mergeGeneratorConfigurations(Supplier<ConfigDto> newConfigInstanceSupplier,
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

        if (getRuleRemark(fieldName) != null) {
            config.setRuleRemark(getRuleRemark(fieldName));
        }

        if (specificConfiguration != null) {
            specificConfiguration.accept(config);
        }

        return config;
    }

    @SuppressWarnings("unchecked")
    protected Consumer<ConfigDto> enumGeneratorSpecificConfig(Class<?> generatedType) {
        return (config) -> {
            EnumConfigDto enumConfig = (EnumConfigDto) config;
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

    protected Consumer<ConfigDto> getTemporalGeneratorSupplier(Class<? extends Temporal> generatedType) {
        return (config) -> {
            DateTimeConfigDto dateTimeConfig = (DateTimeConfigDto) config;
            dateTimeConfig.setGeneratedType(generatedType);
        };
    }

    protected Consumer<ConfigDto> getCollectionGeneratorSupplier(Class<? extends Collection<?>> generatedType,
                                                                 IGenerator<?> elementGenerator) {
        return (config) -> {
            CollectionConfigDto collectionConfig = (CollectionConfigDto) config;
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

    protected Consumer<ConfigDto> getArrayGeneratorSupplier(Class<?> elementType,
                                                            IGenerator<?> elementGenerator) {
        return (config) -> {
            ArrayConfigDto arrayConfigDto = (ArrayConfigDto) config;
            arrayConfigDto.setElementType(elementType);
            if (arrayConfigDto.getElementGenerator() == null) {
                arrayConfigDto.setElementGenerator(elementGenerator);
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected Consumer<ConfigDto>
    getMapGeneratorSupplier(Class<? extends Map<?, ?>> generatedType,
                            IGenerator<?> keyGenerator,
                            IGenerator<?> valueGenerator) {
        return (config) -> {
            MapConfigDto mapConfigDto = (MapConfigDto) config;
            if (mapConfigDto.getMapInstanceSupplier() == null) {
                mapConfigDto.setMapInstanceSupplier(
                        () -> (Map<Object, Object>) createInstance(generatedType)
                );
            }
            if (mapConfigDto.getKeyGenerator() == null) {
                mapConfigDto.setKeyGenerator((IGenerator<Object>) keyGenerator);
            }
            if (mapConfigDto.getValueGenerator() == null) {
                mapConfigDto.setValueGenerator((IGenerator<Object>) valueGenerator);
            }
        };
    }

}
