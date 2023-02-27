package org.laoruga.dtogenerator.config.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;

/**
 * This is a basic configuration for all {@link DtoGenerator} instances.
 *
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoGeneratorStaticConfig {

    private static final ConfigurationHolder INSTANCE;

    static {
        INSTANCE = new ConfigurationHolder(
                new DtoGeneratorFileConfig("dtogenerator.properties"),
                new TypeGeneratorsConfigLazy()
        );
    }

    synchronized public static ConfigurationHolder getInstance() {
        return INSTANCE;
    }

}
