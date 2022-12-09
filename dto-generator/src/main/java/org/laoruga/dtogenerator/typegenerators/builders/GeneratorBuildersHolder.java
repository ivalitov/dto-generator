package org.laoruga.dtogenerator.typegenerators.builders;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 13.05.2022
 */
@RequiredArgsConstructor
public final class GeneratorBuildersHolder {

    @Getter(AccessLevel.PRIVATE)
    private final List<GenBuilderInfo> builderInfoList;

    public Optional<IGeneratorBuilder> getBuilder(Class<?> generatedType) {
        if (generatedType.isEnum()) {
            return getEnumBuilder(generatedType);
        }
        IGeneratorBuilder matchedBuilder = null;
        for (GenBuilderInfo info : getBuilderInfoList()) {

            Class<?> buildersGeneratedType = info.getGeneratedType();

            if (buildersGeneratedType == generatedType) {
                if (matchedBuilder != null) {
                    throw new DtoGeneratorException("More than one matched generators found for generated type: " + "'" + generatedType + "'");
                }
                matchedBuilder = info.getBuilderSupplier().get();
            }

        }
        return matchedBuilder != null ?
                Optional.of(matchedBuilder) :
                Optional.empty();
    }

    private Optional<IGeneratorBuilder> getEnumBuilder(Class<?> generatedType) {
        IGeneratorBuilder anyEnumBuilder = null;
        IGeneratorBuilder strictEnumBuilder = null;

        for (GenBuilderInfo info : getBuilderInfoList()) {

            if (info.getGeneratedType() == Enum.class) {
                if (anyEnumBuilder != null) {
                    throwError(generatedType);
                }
                anyEnumBuilder = info.getBuilderSupplier().get();
            } else if (info.getGeneratedType() == generatedType) {
                if (strictEnumBuilder != null) {
                    throwError(generatedType);
                }
                strictEnumBuilder = info.getBuilderSupplier().get();
            }

        }
        return Optional.ofNullable(anyEnumBuilder != null ? anyEnumBuilder : strictEnumBuilder);
    }

    private static void throwError(Class<?> generatedType) {
        throw new DtoGeneratorException("More than one matched generators found for generated type: " + "'" + generatedType + "'");
    }

    public Optional<IGeneratorBuilder> getBuilder(Annotation rulesAnnotation, Class<?> generatedType) {
        GenBuilderInfo matchedBuilder = null;
        for (GenBuilderInfo info : getBuilderInfoList()) {
            if (info.getRules() == rulesAnnotation.annotationType()) {
                if (matchedBuilder != null) {
                    throw new DtoGeneratorException("More than one matched generators found for rules: " +
                            "'" + rulesAnnotation + "'");
                }

                matchedBuilder = info;
            }
        }
        if (matchedBuilder == null) {
            return Optional.empty();
        }

        Class<?> buildersGeneratedType = matchedBuilder.getGeneratedType();

        if (buildersGeneratedType == generatedType ||
                buildersGeneratedType.isAssignableFrom(generatedType) ||
                (generatedType.isPrimitive() && generatedType == matchedBuilder.getGeneratedTypePrimitive())) {
            return Optional.of(matchedBuilder.builderSupplier.get());
        }
        throw new DtoGeneratorException("For rules: '" + rulesAnnotation + "'" +
                " builder's generated type: '" + buildersGeneratedType + "'" +
                " not matched to field type: " + generatedType + "'.");
    }

    public void addBuilder(Class<? extends Annotation> rulesClass,
                           Class<?> generatedType,
                           IGeneratorBuilder genBuilder) {
        getBuilderInfoList().add(
                GenBuilderInfo.createInstance(rulesClass, generatedType, () -> genBuilder)
        );
    }

    @Getter
    static class GenBuilderInfo {

        private Class<? extends Annotation> rules;
        private Class<?> generatedType;
        private Class<?> generatedTypePrimitive;

        private Supplier<IGeneratorBuilder> builderSupplier;

        public static GenBuilderInfo createInstance(Class<? extends Annotation> rules,
                                                    Class<?> generatedType,
                                                    Supplier<IGeneratorBuilder> builderSupplier) {
            return createInstance(rules, generatedType, null, builderSupplier);
        }

        public static GenBuilderInfo createInstance(Class<? extends Annotation> rules,
                                                    Class<?> generatedType,
                                                    Class<?> generatedTypePrimitive,
                                                    Supplier<IGeneratorBuilder> builderSupplier) {
            GenBuilderInfo genBuilderInfo = new GenBuilderInfo();
            genBuilderInfo.rules = rules;
            genBuilderInfo.generatedType = generatedType;
            genBuilderInfo.generatedTypePrimitive = generatedTypePrimitive;
            genBuilderInfo.builderSupplier = builderSupplier;
            return genBuilderInfo;
        }
    }
}
