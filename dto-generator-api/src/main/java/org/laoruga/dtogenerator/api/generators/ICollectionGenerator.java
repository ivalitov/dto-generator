package org.laoruga.dtogenerator.api.generators;

import java.util.Collection;

/**
 * @author Il'dar Valitov
 * Created on 28.04.2022
 */

public interface ICollectionGenerator<T> extends IGenerator<Collection<T>> {

    IGenerator<T> getItemGenerator();
}
