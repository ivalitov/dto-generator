package laoruga.dtogenerator.api.factory;

import laoruga.dtogenerator.api.markup.generators.IGenerator;

/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
public interface GeneratorFactory {

    IGenerator<?> getGenerator();
}
