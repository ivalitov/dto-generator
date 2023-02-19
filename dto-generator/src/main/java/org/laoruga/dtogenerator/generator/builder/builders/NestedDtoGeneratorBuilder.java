package org.laoruga.dtogenerator.generator.builder.builders;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.generator.NestedDtoGenerator;

import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Slf4j
public class NestedDtoGeneratorBuilder implements IGeneratorBuilder {

    private Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier;

    public NestedDtoGeneratorBuilder setNestedDtoGeneratorSupplier(Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {
        this.nestedDtoGeneratorSupplier = nestedDtoGeneratorSupplier;
        return this;
    }

    @Override
    public NestedDtoGenerator build() {
        return new NestedDtoGenerator(nestedDtoGeneratorSupplier.get());
    }

}
