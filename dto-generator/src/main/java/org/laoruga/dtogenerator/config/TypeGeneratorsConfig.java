package org.laoruga.dtogenerator.config;

import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.generator.builder.builders.*;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.rule.RulesInstance;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */


public class TypeGeneratorsConfig {

    @Getter(AccessLevel.PACKAGE)
    private final
    Map<Class<? extends IGeneratorBuilder>, Map<Class<?>, IConfigDto>>
            configMap = new HashMap<>();

    public IConfigDto getOrNull(Class<?> builderClass, Class<?> generatedType) {

        generatedType = generatedType.isPrimitive() ? Primitives.wrap(generatedType) : generatedType;

        if (configMap.containsKey(builderClass)) {
            Set<Map.Entry<Class<?>, IConfigDto>> generatedTypeConfigSupplierEntrySet
                    = configMap.get(builderClass).entrySet();

            for (Map.Entry<Class<?>, IConfigDto> typeSupplierEntry : generatedTypeConfigSupplierEntrySet) {
                if (typeSupplierEntry.getKey().isAssignableFrom(generatedType)) {
                    return typeSupplierEntry.getValue();
                }
            }

        }

        return null;
    }

    public void setConfig(IConfigDto configDto) {

        Class<? extends IGeneratorBuilder> generatorBuilderClass =
                Objects.requireNonNull(MappingHelper.CONFIG_TYPE_TO_BUILDER_TYPE.get(configDto.getClass()));

        configMap.putIfAbsent(
                generatorBuilderClass,
                new HashMap<>()
        );

        configMap.get(generatorBuilderClass).put(
                MappingHelper.CONFIG_TYPE_TO_GENERATED_TYPE.get(configDto.getClass()),
                configDto
        );
    }

    public void setCollectionConfig(Class<?> superTypeClass,
                                    IConfigDto configDto) {
        Class<? extends IGeneratorBuilder> genBuilderClass = configDto.getBuilderClass();
        configMap.putIfAbsent(genBuilderClass, new HashMap<>());
        configMap.get(genBuilderClass).put(superTypeClass, configDto);
    }

    /*
     * Lazy getters
     */

    public StringConfigDto getStringConfig() {
        return (StringConfigDto) getConfigLazy(
                StringGeneratorBuilder.class,
                RulesInstance.stringRule.generatedType(),
                StringConfigDto::new
        );
    }

    public IntegerConfigDto getIntegerConfig() {
        return (IntegerConfigDto) getConfigLazy(
                IntegerGeneratorBuilder.class,
                RulesInstance.integerRule.generatedType(),
                IntegerConfigDto::new
        );
    }

    public LongConfigDto getLongConfig() {
        return (LongConfigDto) getConfigLazy(
                LongGeneratorBuilder.class,
                RulesInstance.longRule.generatedType(),
                LongConfigDto::new
        );
    }

    public DoubleConfigDto getDoubleConfig() {
        return (DoubleConfigDto) getConfigLazy(
                DoubleGeneratorBuilder.class,
                RulesInstance.doubleRule.generatedType(),
                DoubleConfigDto::new
        );
    }

    public LocalDateTimeConfigDto getLocalDateTimeConfig() {
        return (LocalDateTimeConfigDto) getConfigLazy(
                LocalDateTimeGeneratorBuilder.class,
                RulesInstance.localDateTimeRule.generatedType(),
                LocalDateTimeConfigDto::new
        );
    }

    public EnumConfigDto getEnumConfig() {
        return (EnumConfigDto) getConfigLazy(
                EnumGeneratorBuilder.class,
                RulesInstance.enumRule.generatedType(),
                EnumConfigDto::new
        );
    }

    public CollectionConfigDto getListConfig() {
        IConfigDto config = getOrNull(CollectionGeneratorBuilder.class, List.class);
        if (config == null) {
            config = new CollectionConfigDto();
            setCollectionConfig(List.class, config);
        }
        return (CollectionConfigDto) config;
    }

    public CollectionConfigDto getSetConfig() {
        IConfigDto config = getOrNull(CollectionGeneratorBuilder.class, Set.class);
        if (config == null) {
            config = new CollectionConfigDto();
            setCollectionConfig(Set.class, config);
        }
        return (CollectionConfigDto) config;
    }

    private IConfigDto getConfigLazy(Class<? extends IGeneratorBuilder> genBuilderClass,
                                     Class<?> generatedType,
                                     Supplier<IConfigDto> configSupplier) {
        configMap.putIfAbsent(genBuilderClass, new HashMap<>());
        if (!configMap.get(genBuilderClass).containsKey(generatedType)) {
            IConfigDto configDto = configSupplier.get();
            configMap.get(genBuilderClass).putIfAbsent(generatedType, configDto);
        }
        return configMap.get(genBuilderClass).get(generatedType);
    }

}
