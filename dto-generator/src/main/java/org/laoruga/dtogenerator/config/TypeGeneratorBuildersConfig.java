package org.laoruga.dtogenerator.config;

import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.*;
import org.laoruga.dtogenerator.generator.configs.*;

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
        if (genBuilderClass.isAssignableFrom(CollectionGeneratorBuilder.class)) {
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
            for (Map.Entry<Class<?>, Supplier<IConfigDto>> generatedSuperType : classIConfigDtoMap.entrySet()) {
                if (generatedSuperType.getKey().isAssignableFrom(generatedType)) {
                    if (configDto != null) {
                        throw new DtoGeneratorException("Ambiguous generator's builder config. " +
                                " Found more than one config for generated type: '" + generatedType + "'");
                    }
                    configDto = generatedSuperType.getValue().get();
                }
            }

        }
        return configDto;
    }

    /*
     * Lazy getters
     */

    public StringConfigDto getStringConfig() {
        IConfigDto config = getConfig(StringGeneratorBuilder.class);
        if (config == null) {
            config = new StringConfigDto();
            setConfig(config);
        }
        return (StringConfigDto) config;
    }

    public IntegerConfigDto getIntegerConfig() {
        IConfigDto config = getConfig(IntegerGeneratorBuilder.class);
        if (config == null) {
            config = new IntegerConfigDto();
            setConfig(config);
        }
        return (IntegerConfigDto) config;
    }

    public LongConfigDto getLongConfig() {
        IConfigDto config = getConfig(LongGeneratorBuilder.class);
        if (config == null) {
            config = new LongConfigDto();
            setConfig(config);
        }
        return (LongConfigDto) getConfig(LongGeneratorBuilder.class);
    }

    public DoubleConfigDto getDoubleConfig() {
        IConfigDto config = getConfig(DoubleGeneratorBuilder.class);
        if (config == null) {
            config = new DoubleConfigDto();
            setConfig(config);
        }
        return (DoubleConfigDto) config;
    }

    public LocalDateTimeConfigDto getLocalDateTimeConfig() {
        IConfigDto config = getConfig(LocalDateTimeGeneratorBuilder.class);
        if (config == null) {
            config = new LocalDateTimeConfigDto();
            setConfig(config);
        }
        return (LocalDateTimeConfigDto) config;
    }

    public EnumConfigDto getEnumConfig() {
        IConfigDto config = getConfig(EnumGeneratorBuilder.class);
        if (config == null) {
            config = new EnumConfigDto();
            setConfig(config);
        }
        return (EnumConfigDto) getConfig(EnumGeneratorBuilder.class);
    }

    public CollectionConfigDto getListConfig() {
        IConfigDto config = getConfig(CollectionGeneratorBuilder.class, List.class);
        if (config == null) {
            config = new CollectionConfigDto();
            setCollectionConfig(List.class, config);
        }
        return (CollectionConfigDto) config;
    }

    public CollectionConfigDto getSetConfig() {
        IConfigDto config = getConfig(CollectionGeneratorBuilder.class, Set.class);
        if (config == null) {
            config = new CollectionConfigDto();
            setCollectionConfig(Set.class, config);
        }
        return (CollectionConfigDto) config;
    }

}
