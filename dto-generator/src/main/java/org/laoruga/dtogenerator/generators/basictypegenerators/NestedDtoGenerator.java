package org.laoruga.dtogenerator.generators.basictypegenerators;

import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;

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
