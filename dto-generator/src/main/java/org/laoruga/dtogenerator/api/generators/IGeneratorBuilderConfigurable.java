package org.laoruga.dtogenerator.api.generators;

import org.laoruga.dtogenerator.generator.configs.ConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 18.05.2022
 */

public interface IGeneratorBuilderConfigurable<T> extends IGeneratorBuilder<T> {

    IGenerator<? extends T> build(ConfigDto configDto, boolean merge);
}
