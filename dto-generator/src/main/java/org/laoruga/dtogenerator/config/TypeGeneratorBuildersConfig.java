package org.laoruga.dtogenerator.config;

import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.basictypegenerators.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */

@Getter
public class TypeGeneratorBuildersConfig {

    Map<Class<? extends IGeneratorBuilder>, Supplier<IConfigDto>> configMap = new HashMap<>();
    Map<Class<? extends IGeneratorBuilder>, Map<Class<?>, Supplier<IConfigDto>>> doubleKeyConfigMap = new HashMap<>();

    public void setConfig(IConfigDto configDto) {
        setConfig(configDto.getBuilderClass(), () -> configDto);
    }

    void setConfig(Class<? extends IGeneratorBuilder> genBuilderClass, Supplier<IConfigDto> configDtoSupplier) {
        if (genBuilderClass.isAssignableFrom(CollectionGenerator.CollectionGeneratorBuilder.class)) {
            throw new DtoGeneratorException("For collection builder configuration use 'setCollectionConfig' method.");
        }
        configMap.put(genBuilderClass, configDtoSupplier);
    }

    public void setCollectionConfig(Class<?> superTypeClass,
                                    IConfigDto configDto) {
        setCollectionConfig(superTypeClass, () -> configDto);
    }

    void setCollectionConfig(Class<?> superTypeClass,
                             Supplier<IConfigDto> configDtoSupplier) {
        Class<? extends IGeneratorBuilder> genBuilderClass = configDtoSupplier.get().getBuilderClass();
        doubleKeyConfigMap.putIfAbsent(genBuilderClass, new HashMap<>());
        doubleKeyConfigMap.get(genBuilderClass).put(superTypeClass, configDtoSupplier);
    }

    public IConfigDto getConfig(Class<?> builderClass) {
        IConfigDto configDto= null;
        if (configMap.containsKey(builderClass)) {
            configDto = configMap.get(builderClass).get();
        }
        return configDto;
    }

    public IConfigDto getConfig(Class<?> builderClass, Class<?> generatedType) {
        IConfigDto configDto = null;

        if (configMap.containsKey(builderClass)) {
            configDto = configMap.get(builderClass).get();
        }

        if (doubleKeyConfigMap.containsKey(builderClass)) {

            Map<Class<?>, Supplier<IConfigDto>> classIConfigDtoMap = doubleKeyConfigMap.get(builderClass);
            for (Class<?> generatedSuperType : classIConfigDtoMap.keySet()) {
                if (generatedSuperType.isAssignableFrom(generatedType)) {
                    if (configDto != null) {
                        throw new DtoGeneratorException("Ambiguous generator's builder config. " +
                                " Found more than one config for generated type: '" + generatedType + "'");
                    }
                    configDto = classIConfigDtoMap.get(generatedSuperType).get();
                }
            }

        }
        return configDto;
    }

    /*
     * Lazy getters
     */

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
