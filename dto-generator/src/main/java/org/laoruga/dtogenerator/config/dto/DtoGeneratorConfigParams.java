package org.laoruga.dtogenerator.config.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */
@Getter
@Setter
public class DtoGeneratorConfigParams implements DtoGeneratorConfig {

    private Integer maxDependentGenerationCycles;
    private Integer maxCollectionGenerationCycles;
    private Boolean generateAllKnownTypes;
    private Boolean generateUsersTypes;

}
