package org.laoruga.dtogenerator.api.generators.custom;

import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface ICustomGeneratorDtoDependent<T, V> extends ICustomGenerator<T> {

    void setDtoSupplier(Supplier<V> generatedDto);

    boolean isDtoReady();
}
