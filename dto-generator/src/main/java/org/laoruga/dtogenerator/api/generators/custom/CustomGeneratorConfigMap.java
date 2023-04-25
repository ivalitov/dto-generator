package org.laoruga.dtogenerator.api.generators.custom;

import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.rules.CustomRule;

import java.util.Map;

/**
 * Custom generators based on this interface are able to have Map of String key-value parameters.
 * There are several ways of passing arguments:
 *
 * <ul>
 *     <li>through field annotation {@link CustomRule#args}</li>
 *     <li>for any generator via {@link DtoGeneratorBuilder#addGeneratorParameter(String, String)}</li>
 *     <li>for specific field via {@link DtoGeneratorBuilder#addGeneratorParameter(String, String, String)} </li>
 *     <li>for generators of specific type via {@link DtoGeneratorBuilder#addGeneratorParameter(Class, String, String)} </li>
 * </ul>
 *
 * If no arguments are passed, empty array is injecting.
 *
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface CustomGeneratorConfigMap<T> extends CustomGenerator<T> {

    void setConfigMap(Map<String, String> configMap);

}