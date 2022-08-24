package laoruga.dtogenerator.api.generators;

import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;

/**
 * @author Il'dar Valitov
 * Created on 23.04.2022
 */

public class NestedDtoGenerator<GENERATED_TYPE> implements IGenerator<GENERATED_TYPE> {

    private final DtoGenerator<GENERATED_TYPE> dtoGenerator;

    public NestedDtoGenerator(DtoGenerator<GENERATED_TYPE> dtoGenerator) {
        this.dtoGenerator = dtoGenerator;
    }

    @Override
    public GENERATED_TYPE generate() {
        return dtoGenerator.generateDto();
    }
}
