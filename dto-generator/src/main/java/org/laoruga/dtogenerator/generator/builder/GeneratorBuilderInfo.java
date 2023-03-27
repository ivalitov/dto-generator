package org.laoruga.dtogenerator.generator.builder;

import com.google.common.primitives.Primitives;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 01.02.2023
 */
@Getter
class GeneratorBuilderInfo {

    private Class<? extends Annotation> rules;
    private Class<?> generatedType;
    private Class<?> generatedTypePrimitive;

    @Getter
    private Supplier<IGeneratorBuilder<?>> builderSupplier;

    public static GeneratorBuilderInfo createInstance(Class<? extends Annotation> rules,
                                                      Class<?> generatedType,
                                                      Supplier<IGeneratorBuilder<?>> builderSupplier) {
        GeneratorBuilderInfo genBuilderInfo = new GeneratorBuilderInfo();
        genBuilderInfo.rules = rules;
        genBuilderInfo.generatedType = generatedType;
        genBuilderInfo.generatedTypePrimitive = Primitives.unwrap(generatedType);
        genBuilderInfo.builderSupplier = builderSupplier;
        return genBuilderInfo;
    }

    public static List<GeneratorBuilderInfo> createInstances(Class<? extends Annotation> rules,
                                                             Class<?>[] generatedTypes,
                                                             Supplier<IGeneratorBuilder<?>> builderSupplier) {

        return Arrays.stream(generatedTypes)
                .map(type -> createInstance(rules, type, builderSupplier))
                .collect(Collectors.toList());
    }

}