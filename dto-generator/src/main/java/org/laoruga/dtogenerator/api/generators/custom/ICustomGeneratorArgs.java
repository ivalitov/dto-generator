package org.laoruga.dtogenerator.api.generators.custom;

/**
 * @author Il'dar Valitov
 * Created on 18.04.2022
 */

public interface ICustomGeneratorArgs<T> extends ICustomGenerator<T> {
    
    ICustomGeneratorArgs<T> setArgs(String... args);
}
