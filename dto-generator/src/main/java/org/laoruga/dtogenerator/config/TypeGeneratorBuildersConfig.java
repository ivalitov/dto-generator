package org.laoruga.dtogenerator.config;

import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.basictypegenerators.CollectionGenerator;
import org.laoruga.dtogenerator.generators.basictypegenerators.IConfigDto;

import java.util.HashMap;
import java.util.Map;

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
}
