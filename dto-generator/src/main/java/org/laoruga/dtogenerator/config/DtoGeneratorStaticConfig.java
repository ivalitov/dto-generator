package org.laoruga.dtogenerator.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.DtoGenerator;

/**
 * This is a basic configuration for all {@link DtoGenerator} instances.
 *
 * @author Il'dar Valitov
 * Created on 13.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DtoGeneratorStaticConfig {

    private static final DtoGeneratorConfig instance = new DtoGeneratorFileConfig("dtogenerator.properties");

    synchronized public static DtoGeneratorConfig getInstance() {
        return instance;
    }

}
