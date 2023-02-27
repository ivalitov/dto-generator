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
    public void setMaxDependentGenerationCycles(Integer maxDependentGenerationCycles) {
        INSTANCE_CONFIG.setMaxDependentGenerationCycles(maxDependentGenerationCycles);
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
    public Integer getMaxDependentGenerationCycles() {
        return getInstanceOrStatic(
                INSTANCE_CONFIG.getMaxDependentGenerationCycles(),
                STATIC_CONFIG.getMaxDependentGenerationCycles());
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
