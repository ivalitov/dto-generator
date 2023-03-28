package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.generator.configs.CustomConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 25.11.2022
 -*/
@AllArgsConstructor
public class CustomGenerator implements IGenerator<Object> {

    @Getter
    private final IGenerator<?> usersGeneratorInstance;

    public CustomGenerator(CustomConfigDto config) {
        this.usersGeneratorInstance = config.getCustomGenerator();
    }

    @Override
    public Object generate() {
        return usersGeneratorInstance.generate();
    }

}
