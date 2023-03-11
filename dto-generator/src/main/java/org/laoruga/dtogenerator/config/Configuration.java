package org.laoruga.dtogenerator.config;

import org.laoruga.dtogenerator.config.dto.DtoGeneratorConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;

/**
 * @author Il'dar Valitov
 * Created on 08.03.2023
 */
public interface Configuration {

    DtoGeneratorConfig getDtoGeneratorConfig();

    TypeGeneratorsConfigLazy getTypeGeneratorsConfig();

}
