package org.laoruga.dtogenerator.config;

import org.laoruga.dtogenerator.DtoGenerator;

/**
 * Config decorator for {@link DtoGenerator} instance,
 * provides instance specific config parameter if exists,
 * otherwise - value from static config.
 *
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */
public class DtoGeneratorInstanceConfig extends DtoGeneratorConfig {

    private final DtoGeneratorConfig staticConfig = DtoGeneratorStaticConfig.getInstance();
    private final DtoGeneratorConfig instanceConfig = new DtoGeneratorConfig();

    @Override
    public void setMaxDependentGenerationCycles(Integer maxDependentGenerationCycles) {
        instanceConfig.setMaxDependentGenerationCycles(maxDependentGenerationCycles);
    }

    @Override
    public void setMaxCollectionGenerationCycles(Integer maxCollectionGenerationCycles) {
        instanceConfig.setMaxCollectionGenerationCycles(maxCollectionGenerationCycles);
    }

    @Override
    public void setGenerateAllKnownTypes(Boolean generateAllKnownTypes) {
        instanceConfig.setGenerateAllKnownTypes(generateAllKnownTypes);
    }

    @Override
    public Integer getMaxDependentGenerationCycles() {
        return getInsatnceOrStatic(
                instanceConfig.getMaxDependentGenerationCycles(),
                staticConfig.getMaxDependentGenerationCycles());
    }

    @Override
    public Integer getMaxCollectionGenerationCycles() {
        return getInsatnceOrStatic(
                instanceConfig.getMaxCollectionGenerationCycles(),
                staticConfig.getMaxCollectionGenerationCycles());
    }

    @Override
    public Boolean getGenerateAllKnownTypes() {
        return getInsatnceOrStatic(
                instanceConfig.getGenerateAllKnownTypes(),
                staticConfig.getGenerateAllKnownTypes());
    }

    @Override
    public TypeGeneratorsConfig getGeneratorsConfig() {
        return instanceConfig.getGeneratorsConfig();
    }

    private <T> T getInsatnceOrStatic(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
