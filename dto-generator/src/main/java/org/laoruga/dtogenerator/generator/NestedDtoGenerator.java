package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.generator.configs.NestedConfigDto;

/**
 * @author Il'dar Valitov
 * Created on 23.04.2022
 */
@AllArgsConstructor
public class NestedDtoGenerator implements IGenerator<Object> {

    private final DtoGenerator<?> dtoGenerator;

    public NestedDtoGenerator(NestedConfigDto config) {
        this.dtoGenerator = config.getDtoGenerator();
    }

    @Override
    public Object generate() {
        return dtoGenerator.generateDto();
    }

}
