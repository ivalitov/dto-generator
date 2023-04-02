package org.laoruga.dtogenerator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;

/**
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */
@AllArgsConstructor
public class ConfigurationHolder implements Configuration {

    /**
     * @param source constructor to copy
     */
    public ConfigurationHolder(ConfigurationHolder source) {
        this.dtoGeneratorConfig = source.getDtoGeneratorConfig();
        this.typeGeneratorsConfig = source.getTypeGeneratorsConfig();
        this.typeGeneratorsConfigForField = new TypeGeneratorsConfigForFiled();
    }

    @Getter
    private DtoGeneratorConfig dtoGeneratorConfig;
    @Getter
    private TypeGeneratorsConfigLazy typeGeneratorsConfig;
    @Getter
    private TypeGeneratorsConfigForFiled typeGeneratorsConfigForField;
}
