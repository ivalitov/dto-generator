package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.constants.Boundary;

/**
 * Custom generators based on this interface are able to have {@link Boundary} parameter.
 * Each {@link org.laoruga.dtogenerator.api.rules.meta.Rule Rule} annotation has
 * {@link Boundary boundary} parameter.
 * <p>
 * There are several ways of passing arguments:
 * <ul>
 *     <li>through field annotation {@link org.laoruga.dtogenerator.api.rules.CustomRule#boundary}</li>
 *     <li>for any field via {@link org.laoruga.dtogenerator.DtoGeneratorBuilder#setBoundary(Boundary)}}</li>
 *     <li>for specific field via {@link org.laoruga.dtogenerator.DtoGeneratorBuilder#setBoundary(String, Boundary)}</li>
 * </ul>
 *
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface CustomGeneratorBoundary<T> extends CustomGenerator<T> {

    void setBoundary(Boundary boundary);
}
