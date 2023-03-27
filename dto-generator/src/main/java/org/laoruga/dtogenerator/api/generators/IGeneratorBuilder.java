package org.laoruga.dtogenerator.api.generators;

/**
 * @author Il'dar Valitov
 * Created on 18.05.2022
 */

@FunctionalInterface
public interface IGeneratorBuilder<T> {

    IGenerator<T> build();
}
