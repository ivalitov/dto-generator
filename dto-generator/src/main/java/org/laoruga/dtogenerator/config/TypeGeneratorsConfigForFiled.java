package org.laoruga.dtogenerator.config;

import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 05.03.2023
 */
public class TypeGeneratorsConfigForFiled {

    private final Map<String, Map<Class<? extends ConfigDto>, ConfigDto>> userConfigForField;

    public TypeGeneratorsConfigForFiled() {
        this.userConfigForField = new HashMap<>();
    }

    public synchronized void setGeneratorConfigForField(String fieldName, ConfigDto generatorConfig) {
        Class<? extends ConfigDto> configClass = generatorConfig.getClass();
        userConfigForField.putIfAbsent(fieldName, new HashMap<>());
        if (userConfigForField.get(fieldName).containsKey(configClass)) {
            throw new DtoGeneratorException("Generator config '" + configClass +
                    "' already has been set explicitly for the field: '" + fieldName + "'");
        }
        userConfigForField.get(fieldName).put(configClass, generatorConfig);
    }

    public ConfigDto getOrNull(String fieldName, Class<? extends ConfigDto> configClass) {
        if (userConfigForField.containsKey(fieldName)) {
            return userConfigForField.get(fieldName).get(configClass);
        }
        return null;
    }

}
