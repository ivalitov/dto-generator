package org.laoruga.dtogenerator.generators;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;

import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 23.04.2022
 */
@AllArgsConstructor
public class NestedDtoGenerator implements IGenerator<Object> {

    private final DtoGenerator<?> dtoGenerator;

    public static NestedDtoGenerator.NestedDtoGeneratorBuilder builder() {
        return new NestedDtoGenerator.NestedDtoGeneratorBuilder();
    }

    @Override
    public Object generate() {
        return dtoGenerator.generateDto();
    }

    @Slf4j
    public static class NestedDtoGeneratorBuilder implements IGeneratorBuilder {

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
}
