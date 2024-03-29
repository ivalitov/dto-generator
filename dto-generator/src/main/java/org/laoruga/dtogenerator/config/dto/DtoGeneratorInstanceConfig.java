package org.laoruga.dtogenerator.config.dto;

import org.laoruga.dtogenerator.DtoGenerator;

/**
 * Config decorator for {@link DtoGenerator} instance,
 * provides instance specific config parameter if exists,
 * otherwise - value from static config.
 *
 * @author Il'dar Valitov
 * Created on 27.02.2023
 */
public class DtoGeneratorInstanceConfig implements DtoGeneratorConfig {

    private final DtoGeneratorConfig INSTANCE_CONFIG = new DtoGeneratorConfigParams();
    private final DtoGeneratorConfig STATIC_CONFIG = DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig();

    @Override
    public void setMaxFailuresNumberDuringDtoGeneration(Integer maxFailuresNumberDuringDtoGeneration) {
        INSTANCE_CONFIG.setMaxFailuresNumberDuringDtoGeneration(maxFailuresNumberDuringDtoGeneration);
    }

    @Override
    public void setMaxCollectionGenerationCycles(Integer maxCollectionGenerationCycles) {
        INSTANCE_CONFIG.setMaxCollectionGenerationCycles(maxCollectionGenerationCycles);
    }

    @Override
    public void setGenerateAllKnownTypes(Boolean generateAllKnownTypes) {
        INSTANCE_CONFIG.setGenerateAllKnownTypes(generateAllKnownTypes);
    }

    @Override
    public Boolean getGenerateUsersTypes() {
        return getInstanceOrStatic(
                INSTANCE_CONFIG.getGenerateUsersTypes(),
                STATIC_CONFIG.getGenerateUsersTypes());
    }

    @Override
    public void setGenerateUsersTypes(Boolean generateUsersTypes) {
        INSTANCE_CONFIG.setGenerateUsersTypes(generateUsersTypes);
    }

    @Override
    public Integer getMaxFailuresNumberDuringDtoGeneration() {
        return getInstanceOrStatic(
                INSTANCE_CONFIG.getMaxFailuresNumberDuringDtoGeneration(),
                STATIC_CONFIG.getMaxFailuresNumberDuringDtoGeneration());
    }

    @Override
    public Integer getMaxCollectionGenerationCycles() {
        return getInstanceOrStatic(
                INSTANCE_CONFIG.getMaxCollectionGenerationCycles(),
                STATIC_CONFIG.getMaxCollectionGenerationCycles());
    }

    @Override
    public Boolean getGenerateAllKnownTypes() {
        return getInstanceOrStatic(
                INSTANCE_CONFIG.getGenerateAllKnownTypes(),
                STATIC_CONFIG.getGenerateAllKnownTypes());
    }

    private <T> T getInstanceOrStatic(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
