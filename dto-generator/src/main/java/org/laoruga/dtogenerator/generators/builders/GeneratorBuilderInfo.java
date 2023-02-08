package org.laoruga.dtogenerator.generators.builders;

import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

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
    private Supplier<IGeneratorBuilder> builderSupplier;

    public static GeneratorBuilderInfo createInstance(Class<? extends Annotation> rules,
                                                      Class<?> generatedType,
                                                      Supplier<IGeneratorBuilder> builderSupplier) {
        return createInstance(rules, generatedType, null, builderSupplier);
    }

    public static GeneratorBuilderInfo createInstance(Class<? extends Annotation> rules,
                                                      Class<?> generatedType,
                                                      Class<?> generatedTypePrimitive,
                                                      Supplier<IGeneratorBuilder> builderSupplier) {
        GeneratorBuilderInfo genBuilderInfo = new GeneratorBuilderInfo();
        genBuilderInfo.rules = rules;
        genBuilderInfo.generatedType = generatedType;
        genBuilderInfo.generatedTypePrimitive = generatedTypePrimitive;
        genBuilderInfo.builderSupplier = builderSupplier;
        return genBuilderInfo;
    }
}