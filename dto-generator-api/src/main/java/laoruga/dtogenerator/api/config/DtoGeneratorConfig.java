package laoruga.dtogenerator.api.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoGeneratorConfig {

    private static final DtoGeneratorParams defaults = new DtoGeneratorParams();

    public static int maxDependentGenerationCycles;
    public static int maxCollectionGenerationCycles;

    static {
        maxDependentGenerationCycles = defaults.getMaxDependentGenerationCycles();
        maxCollectionGenerationCycles = defaults.getMaxCollectionGenerationCycles();
    }

}
