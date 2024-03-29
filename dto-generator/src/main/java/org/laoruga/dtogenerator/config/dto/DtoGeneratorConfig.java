package org.laoruga.dtogenerator.config.dto;

/**
 * @author Il'dar Valitov
 * Created on 26.02.2023
 */
public interface DtoGeneratorConfig {

    Integer getMaxFailuresNumberDuringDtoGeneration();

    void setMaxFailuresNumberDuringDtoGeneration(Integer maxFailuresNumberDuringDtoGeneration);

    Integer getMaxCollectionGenerationCycles();

    void setMaxCollectionGenerationCycles(Integer maxCollectionGenerationCycles);

    Boolean getGenerateAllKnownTypes();

    void setGenerateAllKnownTypes(Boolean generateAllKnownTypes);

    Boolean getGenerateUsersTypes();

    void setGenerateUsersTypes(Boolean generateUsersTypes);

}
