package org.laoruga.dtogenerator.api.generators;

import org.laoruga.dtogenerator.generator.configs.IConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 18.05.2022
 */

public interface IGeneratorBuilderConfigurable extends IGeneratorBuilder {

    IGenerator<?> build(IConfigDto configDto, boolean merge);
}
