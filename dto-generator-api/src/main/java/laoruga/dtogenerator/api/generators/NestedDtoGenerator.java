package laoruga.dtogenerator.api.generators;

import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;

public class NestedDtoGenerator<GENERATED_TYPE> implements IGenerator<GENERATED_TYPE> {

    private final DtoGenerator dtoGenerator;
    private final Class<GENERATED_TYPE> nestedDtoType;

    public NestedDtoGenerator(DtoGenerator dtoGenerator, Class<GENERATED_TYPE> nestedDtoType) {
        this.dtoGenerator = dtoGenerator;
        this.nestedDtoType = nestedDtoType;
    }

    @Override
    public GENERATED_TYPE generate() {
        return dtoGenerator.generateDto(nestedDtoType);
    }
}
