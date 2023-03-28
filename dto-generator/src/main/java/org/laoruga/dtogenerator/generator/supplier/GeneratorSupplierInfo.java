package org.laoruga.dtogenerator.generator.supplier;

import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.generator.configs.ConfigDto;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 01.02.2023
 */
@Getter
class GeneratorSupplierInfo {

    private Class<? extends Annotation> rules;
    private Class<?> generatedType;
    private Function<ConfigDto, IGenerator<?>> generatorSupplier;

    static GeneratorSupplierInfo createInstance(Class<? extends Annotation> rules,
                                                       Class<?> generatedType,
                                                       Function<ConfigDto, IGenerator<?>> builderSupplier) {
        GeneratorSupplierInfo genBuilderInfo = new GeneratorSupplierInfo();
        genBuilderInfo.rules = rules;
        genBuilderInfo.generatedType = generatedType;
        genBuilderInfo.generatorSupplier = builderSupplier;
        return genBuilderInfo;
    }

    static List<GeneratorSupplierInfo> createInstances(Class<? extends Annotation> rules,
                                                              Function<ConfigDto, IGenerator<?>> builderSupplier) {
        return Arrays.stream(GeneratedTypes.get(rules))
                .map(type -> createInstance(rules, type, builderSupplier))
                .collect(Collectors.toList());
    }

}