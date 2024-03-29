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

    @Getter
    private DtoGeneratorConfig dtoGeneratorConfig;
    @Getter
    private TypeGeneratorsConfigLazy typeGeneratorsConfig;
    @Getter
    private TypeGeneratorsConfigForFiled typeGeneratorsConfigForField;
    @Getter
    private final CustomGeneratorsConfigurationHolder customGeneratorsConfigurators;

    /*
     * Constructor to copy
     */
    public ConfigurationHolder(DtoGeneratorConfig dtoGeneratorConfig,
                               TypeGeneratorsConfigLazy typeGeneratorsConfig,
                               CustomGeneratorsConfigurationHolder customGeneratorsConfigurators) {
        this.dtoGeneratorConfig = dtoGeneratorConfig;
        this.typeGeneratorsConfig = typeGeneratorsConfig;
        this.typeGeneratorsConfigForField = new TypeGeneratorsConfigForFiled();
        this.customGeneratorsConfigurators = customGeneratorsConfigurators;
    }
}
