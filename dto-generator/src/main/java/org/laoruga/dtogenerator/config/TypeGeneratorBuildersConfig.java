package org.laoruga.dtogenerator.config;

import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
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

    protected void setConfig(Class<? extends IGeneratorBuilder> collectionGenBuilderClass,
                             Class<?> superTypeClass,
                             IConfigDto configDto) {
        doubleKeyConfigMap.putIfAbsent(collectionGenBuilderClass, new HashMap<>());
        doubleKeyConfigMap.get(collectionGenBuilderClass).put(superTypeClass, configDto);
    }

    protected void setConfig(Class<? extends IGeneratorBuilder> genBuilderClass,
                             IConfigDto configDto) {
        configMap.put(genBuilderClass, configDto);
    }

    public IConfigDto getConfig(Class<?> builderClass) {
        return configMap.get(builderClass);
    }

    public IConfigDto getConfig(Class<?> builderClass, Class<?> generatedType) {
        IConfigDto configDto = null;

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
