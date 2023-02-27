package org.laoruga.dtogenerator.config.types;

import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.generator.builder.builders.*;
import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.rule.RulesInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Provides lazy getters of config instances containing default configuration (default values from annotations).
 *
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */


public class TypeGeneratorsConfigLazy implements TypeGeneratorsConfigSupplier {

    @Getter(AccessLevel.PACKAGE)
    private final
    Map<Class<? extends IGeneratorBuilder>, Map<Class<?>, ConfigDto>>
            configMap = new HashMap<>();

    /**
     * @param builderClass  type generator builder class
     * @param generatedType field type for which the value is supposed to be generated
     *
     * @return config instance or null if there is no config found according to the given classes
     */
    public ConfigDto getOrNull(Class<?> builderClass, Class<?> generatedType) {

        generatedType = generatedType.isPrimitive() ? Primitives.wrap(generatedType) : generatedType;

        if (configMap.containsKey(builderClass)) {
            Set<Map.Entry<Class<?>, ConfigDto>> generatedTypeConfigSupplierEntrySet
                    = configMap.get(builderClass).entrySet();

            for (Map.Entry<Class<?>, ConfigDto> typeSupplierEntry : generatedTypeConfigSupplierEntrySet) {
                if (typeSupplierEntry.getKey().isAssignableFrom(generatedType)) {
                    return typeSupplierEntry.getValue();
                }
            }

        }

        return null;
    }

    public void setCollectionConfig(Class<?> superTypeClass,
                                    ConfigDto configDto) {
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
        ConfigDto config = getOrNull(CollectionGeneratorBuilder.class, List.class);
        if (config == null) {
            config = new CollectionConfigDto();
            setCollectionConfig(List.class, config);
        }
        return (CollectionConfigDto) config;
    }

    public CollectionConfigDto getSetConfig() {
        ConfigDto config = getOrNull(CollectionGeneratorBuilder.class, Set.class);
        if (config == null) {
            config = new CollectionConfigDto();
            setCollectionConfig(Set.class, config);
        }
        return (CollectionConfigDto) config;
    }

    private ConfigDto getConfigLazy(Class<? extends IGeneratorBuilder> genBuilderClass,
                                    Class<?> generatedType,
                                    Supplier<ConfigDto> configSupplier) {
        configMap.putIfAbsent(genBuilderClass, new HashMap<>());
        if (!configMap.get(genBuilderClass).containsKey(generatedType)) {
            ConfigDto configDto = configSupplier.get();
            configMap.get(genBuilderClass).putIfAbsent(generatedType, configDto);
        }
        return configMap.get(genBuilderClass).get(generatedType);
    }

}
