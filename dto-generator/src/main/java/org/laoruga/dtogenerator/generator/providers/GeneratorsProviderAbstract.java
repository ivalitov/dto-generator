package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.EnumGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.CollectionConfigDto;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;
import org.laoruga.dtogenerator.generator.configs.EnumConfigDto;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createCollectionInstance;

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

        ConfigDto staticConfig = DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig()
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

    protected BiFunction<ConfigDto, IGeneratorBuilderConfigurable<?>,
            IGenerator<?>> collectionGeneratorSupplier(Class<? extends Collection<?>> generatedType, IGenerator<?> elementGenerator) {
        return (config, builder) -> {
            CollectionConfigDto collectionConfig = (CollectionConfigDto) config;
            if (collectionConfig.getCollectionInstanceSupplier() == null) {
                collectionConfig.setCollectionInstanceSupplier(
                        () -> createCollectionInstance(generatedType)
                );
            }
            ((CollectionConfigDto) config).setElementGenerator(elementGenerator);
            return builder.build(config, true);
        };
    }

    public static Class<?> getConcreteCollectionClass(Class<? extends Collection<?>> fieldType) {

        if (!Modifier.isInterface(fieldType.getModifiers()) && !Modifier.isAbstract(fieldType.getModifiers())) {
            return fieldType;
        }

        if (List.class.isAssignableFrom(fieldType)) {
            return ArrayList.class;
        } else if (Set.class.isAssignableFrom(fieldType)) {
            return HashSet.class;
        } else if (Queue.class.isAssignableFrom(fieldType)) {
            return PriorityQueue.class;
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            return ArrayList.class;
        } else {
            throw new DtoGeneratorException("Unsupported collection type: '" + fieldType.getTypeName() + "'");
        }

    }
}
