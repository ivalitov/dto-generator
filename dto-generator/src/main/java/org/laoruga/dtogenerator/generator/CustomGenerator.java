package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.generator.config.dto.CustomConfig;

/**
 * @author Il'dar Valitov
 * Created on 25.11.2022
 -*/
@AllArgsConstructor
public class CustomGenerator implements Generator<Object> {

    @Getter
    private final Generator<?> usersGeneratorInstance;

    public CustomGenerator(CustomConfig config) {
        this.usersGeneratorInstance = config.getCustomGenerator();
    }

    @Override
    public Object generate() {
        return usersGeneratorInstance.generate();
    }

}
