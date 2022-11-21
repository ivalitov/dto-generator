package org.laoruga.dtogenerator.api.generators;

/**
 * @author Il'dar Valitov
 * Created on 18.04.2022
 */

public interface ICustomGeneratorArgs<T> extends ICustomGenerator<T> {
    
    ICustomGeneratorArgs<T> setArgs(String... args);
}
