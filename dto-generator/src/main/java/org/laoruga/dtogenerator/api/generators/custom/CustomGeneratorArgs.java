package org.laoruga.dtogenerator.api.generators.custom;

/**
 * @author Il'dar Valitov
 * Created on 18.04.2022
 */

public interface CustomGeneratorArgs<T> extends CustomGenerator<T> {
    
    void setArgs(String... args);
}
