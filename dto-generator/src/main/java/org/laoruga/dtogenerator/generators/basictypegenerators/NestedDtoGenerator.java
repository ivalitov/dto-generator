package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author Il'dar Valitov
 * Created on 23.04.2022
 */
@AllArgsConstructor
public class NestedDtoGenerator implements IGenerator<Object> {

    private final DtoGenerator<?> nestedDtoGenerator;

    public static NestedDtoGenerator.NestedDtoGeneratorBuilder builder() {
        return new NestedDtoGenerator.NestedDtoGeneratorBuilder();
    }

    @Override
    public Object generate() {
        return nestedDtoGenerator.generateDto();
    }

    @Slf4j
    public static class NestedDtoGeneratorBuilder implements IGeneratorBuilder {

        private Field field;
        private String[] fieldsPath;
        private DtoGeneratorBuilder.GeneratorBuildersTree generatorBuildersTree;

        public NestedDtoGenerator.NestedDtoGeneratorBuilder setField(Field field) {
            this.field = field;
            return this;
        }

        public NestedDtoGenerator.NestedDtoGeneratorBuilder setFieldsPath(String[] fieldsPath) {
            this.fieldsPath = fieldsPath;
            return this;
        }

        public NestedDtoGenerator.NestedDtoGeneratorBuilder setGeneratorBuildersTree(
                DtoGeneratorBuilder.GeneratorBuildersTree generatorBuildersTree) {
            this.generatorBuildersTree = generatorBuildersTree;
            return this;
        }

        DtoGenerator<?> createNestedDtoGenerator() {
            String[] pathToNestedDtoField = Arrays.copyOf(fieldsPath, fieldsPath.length + 1);
            pathToNestedDtoField[fieldsPath.length] = field.getName();
            DtoGeneratorBuilder<?> nestedDtoGeneratorBuilder = generatorBuildersTree.getBuilder(pathToNestedDtoField);
            return nestedDtoGeneratorBuilder.buildNestedFieldGenerator(pathToNestedDtoField, field.getType());
        }

        @Override
        public NestedDtoGenerator build() {
            return new NestedDtoGenerator(createNestedDtoGenerator());
        }

    }
}
