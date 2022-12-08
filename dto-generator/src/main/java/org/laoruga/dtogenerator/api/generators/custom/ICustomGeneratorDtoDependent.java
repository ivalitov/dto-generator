package org.laoruga.dtogenerator.api.generators.custom;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface ICustomGeneratorDtoDependent<T, V> extends ICustomGenerator<T> {

    void setDto(V generatedDto);

    boolean isDtoReady();
}
