package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.Generator;

/**
 * @author Il'dar Valitov
 * Created on 25.11.2022
 * -
 */
@AllArgsConstructor
public class CustomGeneratorWrapper implements Generator<Object> {

    @Getter
    private final Generator<?> usersGeneratorInstance;

    @Override
    public Object generate() {
        return usersGeneratorInstance.generate();
    }

}
