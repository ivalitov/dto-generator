package org.laoruga.dtogenerator.api.generators.custom;

/**
 * Custom generators based on this interface are able to have an array of String arguments.
 * There are several ways of passing arguments:
 *
 * <ul>
 *     <li>via {@link org.laoruga.dtogenerator.api.rules.CustomRule#args} method on the annotated field</li>
 *     <li>for specific field via {@link org.laoruga.dtogenerator.DtoGeneratorBuilder#setGeneratorArgs(String, String...)}</li>
 *     <li>for generators of specific type via {@link org.laoruga.dtogenerator.DtoGeneratorBuilder#setGeneratorArgs(Class, String...)} (String, String...)}</li>
 *     <li>along with generator via {@link org.laoruga.dtogenerator.DtoGeneratorBuilder#setGenerator(String, CustomGeneratorArgs, String...)}
 *     or
 *     {@link org.laoruga.dtogenerator.DtoGeneratorBuilder#setGenerator(Class, CustomGeneratorArgs, String...)}</li>
 * </ul>
 * If no arguments are passed, empty array is injecting.
 *
 * @author Il'dar Valitov
 * Created on 18.04.2022
 */

public interface CustomGeneratorArgs<T> extends CustomGenerator<T> {

    /**
     * @param args args to inject into custom generator instance
     */
    void setArgs(String... args);
}
