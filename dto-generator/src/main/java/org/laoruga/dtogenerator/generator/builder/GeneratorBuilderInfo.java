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
        return createInstance(rules, generatedType, null, builderSupplier);
    }

    public static GeneratorBuilderInfo createInstance(Class<? extends Annotation> rules,
                                                      Class<?> generatedType,
                                                      Class<?> generatedTypePrimitive,
                                                      Supplier<IGeneratorBuilder<?>> builderSupplier) {
        GeneratorBuilderInfo genBuilderInfo = new GeneratorBuilderInfo();
        genBuilderInfo.rules = rules;
        genBuilderInfo.generatedType = generatedType;
        genBuilderInfo.generatedTypePrimitive = generatedTypePrimitive;
        genBuilderInfo.builderSupplier = builderSupplier;
        return genBuilderInfo;
    }

    public static List<GeneratorBuilderInfo> createInstances(Class<? extends Annotation> rules,
                                                             Class<?>[] generatedType,
                                                             Supplier<IGeneratorBuilder<?>> builderSupplier) {

        return Arrays.stream(generatedType)
                .map(
                        type -> {
                            GeneratorBuilderInfo genBuilderInfo = new GeneratorBuilderInfo();
                            genBuilderInfo.rules = rules;
                            genBuilderInfo.generatedType = type;
                            genBuilderInfo.generatedTypePrimitive = Primitives.unwrap(type);
                            genBuilderInfo.builderSupplier = builderSupplier;
                            return genBuilderInfo;
                        }
                )
                .collect(Collectors.toList());
    }


}