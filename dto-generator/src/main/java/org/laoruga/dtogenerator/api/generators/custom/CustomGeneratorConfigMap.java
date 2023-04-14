package org.laoruga.dtogenerator.api.generators.custom;

import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface CustomGeneratorConfigMap<T> extends CustomGenerator<T> {

    void setConfigMap(Map<String, String> configMap);

}