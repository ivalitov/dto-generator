package org.laoruga.dtogenerator.api.generators;

/**
 * @author Il'dar Valitov
 * Created on 28.04.2022
 */

public interface ICollectionGenerator extends IGenerator<Object> {

    IGenerator<?> getElementGenerator();
}
