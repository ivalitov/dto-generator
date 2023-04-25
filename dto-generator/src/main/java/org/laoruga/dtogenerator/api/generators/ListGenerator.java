package org.laoruga.dtogenerator.api.generators;

/**
 * @author Il'dar Valitov
 * Created on 28.04.2022
 */

public interface ListGenerator extends Generator<Object> {

    /**
     * @return generator of list elements
     */
    Generator<?> getElementGenerator();
}
