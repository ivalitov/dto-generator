package org.laoruga.dtogenerator.config;

import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.basictypegenerators.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */

@Getter
public class TypeGeneratorBuildersConfig {

    Map<Class<? extends IGeneratorBuilder>, IConfigDto> configMap = new HashMap<>();
    Map<Class<? extends IGeneratorBuilder>, Map<Class<?>, IConfigDto>> doubleKeyConfigMap = new HashMap<>();

    public void setConfig(IConfigDto configDto) {
        setConfig(configDto.getBuilderClass(), configDto);
    }

    void setConfig(Class<? extends IGeneratorBuilder> genBuilderClass, IConfigDto configDto) {
        if (genBuilderClass.isAssignableFrom(CollectionGenerator.CollectionGeneratorBuilder.class)) {
            throw new DtoGeneratorException("For collection builder configuration use 'setCollectionConfig' method.");
        }
        configMap.put(genBuilderClass, configDto);
    }

    public void setCollectionConfig(Class<?> superTypeClass,
                                    IConfigDto configDto) {
        Class<? extends IGeneratorBuilder> genBuilderClass = configDto.getBuilderClass();
        doubleKeyConfigMap.putIfAbsent(genBuilderClass, new HashMap<>());
        doubleKeyConfigMap.get(genBuilderClass).put(superTypeClass, configDto);
    }

    public IConfigDto getConfig(Class<?> builderClass) {
        return configMap.get(builderClass);
    }

    public IConfigDto getConfig(Class<?> builderClass, Class<?> generatedType) {
        IConfigDto configDto = null;

        if (configMap.containsKey(builderClass)) {
            configDto = configMap.get(builderClass);
        }

        if (doubleKeyConfigMap.containsKey(builderClass)) {

            Map<Class<?>, IConfigDto> classIConfigDtoMap = doubleKeyConfigMap.get(builderClass);
            for (Class<?> generatedSuperType : classIConfigDtoMap.keySet()) {
                if (generatedSuperType.isAssignableFrom(generatedType)) {
                    if (configDto != null) {
                        throw new DtoGeneratorException("Ambiguous generator's builder config. " +
                                " Found more than one config for generated type: '" + generatedType + "'");
                    }
                    configDto = classIConfigDtoMap.get(generatedSuperType);
                }
            }

        }
        return configDto;
    }

    public StringGenerator.ConfigDto getStringConfig() {
        IConfigDto config = getConfig(StringGenerator.StringGeneratorBuilder.class);
        if (config == null) {
            config = new StringGenerator.ConfigDto();
            setConfig(config);
        }
        return (StringGenerator.ConfigDto) config;
    }

    public IntegerGenerator.ConfigDto getIntegerConfig() {
        IConfigDto config = getConfig(IntegerGenerator.IntegerGeneratorBuilder.class);
        if (config == null) {
            config = new IntegerGenerator.ConfigDto();
            setConfig(config);
        }
        return (IntegerGenerator.ConfigDto) config;
    }

    public LongGenerator.ConfigDto getLongConfig() {
        IConfigDto config = getConfig(LongGenerator.LongGeneratorBuilder.class);
        if (config == null) {
            config = new LongGenerator.ConfigDto();
            setConfig(config);
        }
        return (LongGenerator.ConfigDto) getConfig(LongGenerator.LongGeneratorBuilder.class);
    }

    public DoubleGenerator.ConfigDto getDoubleConfig() {
        IConfigDto config = getConfig(DoubleGenerator.DoubleGeneratorBuilder.class);
        if (config == null) {
            config = new DoubleGenerator.ConfigDto();
            setConfig(config);
        }
        return (DoubleGenerator.ConfigDto) config;
    }

    public LocalDateTimeGenerator.ConfigDto getLocalDateTimeConfig() {
        IConfigDto config = getConfig(LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder.class);
        if (config == null) {
            config = new LocalDateTimeGenerator.ConfigDto();
            setConfig(config);
        }
        return (LocalDateTimeGenerator.ConfigDto) config;
    }

    public EnumGenerator.ConfigDto getEnumConfig() {
        IConfigDto config = getConfig(EnumGenerator.EnumGeneratorBuilder.class);
        if (config == null) {
            config = new EnumGenerator.ConfigDto();
            setConfig(config);
        }
        return (EnumGenerator.ConfigDto) getConfig(EnumGenerator.EnumGeneratorBuilder.class);
    }

    public CollectionGenerator.ConfigDto getListConfig() {
        IConfigDto config = getConfig(CollectionGenerator.CollectionGeneratorBuilder.class, List.class);
        if (config == null) {
            config = new CollectionGenerator.ConfigDto();
            setCollectionConfig(List.class, config);
        }
        return (CollectionGenerator.ConfigDto) config;
    }

    public CollectionGenerator.ConfigDto getSetConfig() {
        IConfigDto config = getConfig(CollectionGenerator.CollectionGeneratorBuilder.class, Set.class);
        if (config == null) {
            config = new CollectionGenerator.ConfigDto();
            setCollectionConfig(Set.class, config);
        }
        return (CollectionGenerator.ConfigDto) config;
    }

}
