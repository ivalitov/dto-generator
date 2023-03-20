package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.EnumGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.EnumConfigDto;
import org.laoruga.dtogenerator.generator.configs.MapConfigDto;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstanceOfConcreteClass;
import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstanceOfConcreteClassAsObject;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
public abstract class GeneratorsProviderAbstract {

    @Getter(AccessLevel.PROTECTED)
    private final ConfigurationHolder configuration;
    @Getter(AccessLevel.PROTECTED)
    private final RemarksHolder remarksHolder;

    protected GeneratorsProviderAbstract(ConfigurationHolder configuration, RemarksHolder remarksHolder) {
        this.configuration = configuration;
        this.remarksHolder = remarksHolder;
    }

    public IRuleRemark getRuleRemark(String fieldName) {
        return remarksHolder.getBasicRemarks().isBasicRuleRemarkExists(fieldName) ?
                remarksHolder.getBasicRemarks().getBasicRuleRemark(fieldName) : null;
    }

    protected IGenerator<?> getGenerator(Supplier<ConfigDto> configDtoSupplier,
                                         Supplier<IGeneratorBuilderConfigurable<?>> genBuildSupplier,
                                         BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>> generatorSupplier,
                                         Class<?> fieldType,
                                         String fieldName) {
        IGeneratorBuilderConfigurable<?> genBuilder = genBuildSupplier.get();

        ConfigDto config = configDtoSupplier.get();

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

        return generatorSupplier.apply(config, genBuilder);
    }

    @SuppressWarnings("unchecked")
    protected BiFunction<
            ConfigDto,
            IGeneratorBuilderConfigurable<?>,
            IGenerator<?>> enumGeneratorSupplier(Class<?> generatedType) {
        return (config, builder) -> {
            EnumConfigDto enumConfig = (EnumConfigDto) config;
            if (enumConfig.getEnumClass() == null) {
                if (generatedType.isEnum()) {
                    enumConfig.setEnumClass((Class<? extends Enum<?>>) generatedType);
                } else {
                    throw new DtoGeneratorException("Field type must be enum : '"
                            + generatedType + "'");
                }
            }
            return ((EnumGeneratorBuilder) builder).build(config, true);
        };
    }

    protected BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>>
    getTemporalGeneratorSupplier(Class<? extends Temporal> generatedType) {
        return (config, builder) -> {
            DateTimeConfigDto dateTimeConfig = (DateTimeConfigDto) config;
            dateTimeConfig.setGeneratedType(generatedType);
            return builder.build(config, true);
        };
    }

    protected BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>,
            IGenerator<?>> getCollectionGeneratorSupplier(Class<? extends Collection<?>> generatedType,
                                                          IGenerator<?> elementGenerator) {
        return (config, builder) -> {
            CollectionConfigDto collectionConfig = (CollectionConfigDto) config;
            if (collectionConfig.getCollectionInstanceSupplier() == null) {
                collectionConfig.setCollectionInstanceSupplier(
                        () -> createInstanceOfConcreteClass(generatedType)
                );
            }
            ((CollectionConfigDto) config).setElementGenerator(elementGenerator);
            return builder.build(config, true);
        };
    }

    @SuppressWarnings("unchecked")
    protected BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>, IGenerator<?>>
    getMapGeneratorSupplier(Class<? extends Map<?, ?>> generatedType,
                            IGenerator<?> keyGenerator,
                            IGenerator<?> valueGenerator) {
        return (config, builder) -> {
            MapConfigDto mapConfigDto = (MapConfigDto) config;
            if (mapConfigDto.getMapInstanceSupplier() == null) {
                mapConfigDto.setMapInstanceSupplier(
                        () -> (Map<Object, Object>) createInstanceOfConcreteClassAsObject(generatedType)
                );
            }
            if (mapConfigDto.getKeyGenerator() == null) {
                mapConfigDto.setKeyGenerator((IGenerator<Object>) keyGenerator);
            }
            if (mapConfigDto.getValueGenerator() == null) {
                mapConfigDto.setValueGenerator((IGenerator<Object>) valueGenerator);
            }
            return builder.build(config, true);
        };
    }

}
