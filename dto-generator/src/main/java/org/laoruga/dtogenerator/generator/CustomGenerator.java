package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.generator.builder.builders.CustomGeneratorBuilder;

/**
 * @author Il'dar Valitov
 * Created on 25.11.2022
 */
@AllArgsConstructor
public class CustomGenerator implements IGenerator<Object> {

    @Getter
    private final IGenerator<?> usersGeneratorInstance;

    public static CustomGeneratorBuilder builder() {
        return new CustomGeneratorBuilder();
    }

    @Override
    public Object generate() {
        return usersGeneratorInstance.generate();
    }

}
