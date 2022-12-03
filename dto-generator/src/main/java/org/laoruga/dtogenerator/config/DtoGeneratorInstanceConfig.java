package org.laoruga.dtogenerator.config;

/**
 * @author Il'dar Valitov
 * Created on 29.11.2022
 */
public class DtoGeneratorInstanceConfig {

    private final DtoGeneratorConfig staticConfig = DtoGeneratorStaticConfig.getInstance();
    private final DtoGeneratorConfig instanceConfig = new DtoGeneratorConfig();

    public void setMaxDependentGenerationCycles(Integer maxDependentGenerationCycles) {
        instanceConfig.setMaxDependentGenerationCycles(maxDependentGenerationCycles);
    }

    public void setMaxCollectionGenerationCycles(Integer maxCollectionGenerationCycles) {
        instanceConfig.setMaxCollectionGenerationCycles(maxCollectionGenerationCycles);
    }

    public void setGenerateAllKnownTypes(Boolean generateAllKnownTypes) {
        instanceConfig.setGenerateAllKnownTypes(generateAllKnownTypes);
    }

    public Integer getMaxDependentGenerationCycles() {
        return getOrDefault(
                instanceConfig.getMaxDependentGenerationCycles(),
                staticConfig.getMaxDependentGenerationCycles());
    }

    public Integer getMaxCollectionGenerationCycles() {
        return getOrDefault(
                instanceConfig.getMaxCollectionGenerationCycles(),
                staticConfig.getMaxCollectionGenerationCycles());
    }

    public Boolean getGenerateAllKnownTypes() {
        return getOrDefault(
                instanceConfig.getGenerateAllKnownTypes(),
                staticConfig.getGenerateAllKnownTypes());
    }

    public TypeGeneratorBuildersConfig getGenBuildersConfig() {
        return instanceConfig.getGenBuildersConfig();
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
