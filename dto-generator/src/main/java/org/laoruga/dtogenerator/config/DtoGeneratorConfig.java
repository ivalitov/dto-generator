package org.laoruga.dtogenerator.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */
@Getter
@Setter
public class DtoGeneratorConfig {

    private Integer maxDependentGenerationCycles;
    private Integer maxCollectionGenerationCycles;
    private Boolean generateAllKnownTypes;

    @Getter(lazy = true)
    private final TypeGeneratorsConfig generatorsConfig = new TypeGeneratorsConfig();
}
