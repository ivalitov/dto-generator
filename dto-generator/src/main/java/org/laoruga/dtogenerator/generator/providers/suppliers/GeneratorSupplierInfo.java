package org.laoruga.dtogenerator.generator.providers.suppliers;

import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;

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
public class GeneratorSupplierInfo {

    private Class<? extends Annotation> rules;
    private Class<?> generatedType;
    private Function<ConfigDto, Generator<?>> generatorSupplier;
    private String[] customGeneratorArgs;

    static GeneratorSupplierInfo createInstance(Class<? extends Annotation> rules,
                                                Class<?> generatedType,
                                                Function<ConfigDto, Generator<?>> builderSupplier,
                                                String... customGeneratorArgs) {
        GeneratorSupplierInfo genBuilderInfo = new GeneratorSupplierInfo();
        genBuilderInfo.rules = rules;
        genBuilderInfo.generatedType = generatedType;
        genBuilderInfo.generatorSupplier = builderSupplier;
        genBuilderInfo.customGeneratorArgs = customGeneratorArgs;
        return genBuilderInfo;
    }

    static List<GeneratorSupplierInfo> createInstances(Class<? extends Annotation> rules,
                                                       Function<ConfigDto, Generator<?>> builderSupplier) {
        return Arrays.stream(GeneratedTypes.get(rules))
                .map(type -> createInstance(rules, type, builderSupplier))
                .collect(Collectors.toList());
    }
}