package org.laoruga.dtogenerator.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoGeneratorConfig {

    private static final DtoGeneratorParams defaults = new DtoGeneratorParams();

    @Setter
    @Getter
    private static int maxDependentGenerationCycles;
    @Setter
    @Getter
    private static int maxCollectionGenerationCycles;
    @Setter
    @Getter
    private static boolean generateAllKnownTypes;

    static {
        maxDependentGenerationCycles = defaults.getMaxDependentGenerationCycles();
        maxCollectionGenerationCycles = defaults.getMaxCollectionGenerationCycles();
        generateAllKnownTypes = defaults.isGenerateAllKnownTypes();
    }

}
