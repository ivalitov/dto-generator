package laoruga.dtogenerator.api.generators;

import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;

/**
 * @author Il'dar Valitov
 * Created on 23.04.2022
 */

public class NestedDtoGenerator<T> implements IGenerator<T> {

    private final DtoGenerator<T> dtoGenerator;

    public NestedDtoGenerator(DtoGenerator<T> dtoGenerator) {
        this.dtoGenerator = dtoGenerator;
    }

    @Override
    public T generate() {
        return dtoGenerator.generateDto();
    }
}
