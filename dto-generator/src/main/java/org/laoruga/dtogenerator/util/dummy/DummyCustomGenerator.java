package org.laoruga.dtogenerator.util.dummy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorConfigMap;

import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 08.02.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DummyCustomGenerator implements CustomGeneratorConfigMap<Object> {

    @Override
    public Object generate() {
        throw new IllegalStateException("This class is not supposed to be instantiated!");
    }

    @Override
    public void setConfigMap(Map<String, String> configMap) {
        throw new IllegalStateException("This class is not supposed to be instantiated!");
    }
}
