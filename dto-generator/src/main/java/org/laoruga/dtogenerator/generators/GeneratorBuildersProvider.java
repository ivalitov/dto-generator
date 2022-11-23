package org.laoruga.dtogenerator.generators;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.basictypegenerators.*;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 13.05.2022
 */
public final class GeneratorBuildersProvider {

    private static GeneratorBuildersProvider instance;

    @Getter(AccessLevel.PRIVATE)
    private final List<GenBuilderInfo> basicBuilderInfoList;
    @Getter(AccessLevel.PRIVATE)
    private final List<RuleInfo> otherRulesInfoList;

    private GeneratorBuildersProvider() {
        basicBuilderInfoList = Arrays.asList(
                GenBuilderInfo.createInstance(StringRule.class, String.class, GeneratorBuildersProvider::stringBuilder),
                GenBuilderInfo.createInstance(IntegerRule.class, Integer.class, GeneratorBuildersProvider::integerBuilder),
                GenBuilderInfo.createInstance(IntegerRule.class, Integer.TYPE, GeneratorBuildersProvider::integerBuilder),
                GenBuilderInfo.createInstance(DoubleRule.class, Double.class, GeneratorBuildersProvider::doubleBuilder),
                GenBuilderInfo.createInstance(DoubleRule.class, Double.TYPE, GeneratorBuildersProvider::doubleBuilder),
                GenBuilderInfo.createInstance(LongRule.class, Long.class, GeneratorBuildersProvider::longBuilder),
                GenBuilderInfo.createInstance(LongRule.class, Long.TYPE, GeneratorBuildersProvider::longBuilder),
                GenBuilderInfo.createInstance(EnumRule.class, Enum.class, GeneratorBuildersProvider::enumBuilder),
                GenBuilderInfo.createInstance(LocalDateTimeRule.class, LocalDateTime.class, GeneratorBuildersProvider::localDateTimeBuilder),

                GenBuilderInfo.createInstance(SetRule.class, Set.class, GeneratorBuildersProvider::collectionBuilder),
                GenBuilderInfo.createInstance(ListRule.class, List.class, GeneratorBuildersProvider::collectionBuilder)
        );
        otherRulesInfoList = Arrays.asList(
                RuleInfo.createInstance(NestedDtoRules.class, Object.class),
                RuleInfo.createInstance(CustomRule.class, Object.class)
        );

    }

    public static synchronized GeneratorBuildersProvider getInstance() {
        if (instance == null) {
            instance = new GeneratorBuildersProvider();
        }
        return instance;
    }

    public Optional<IGeneratorBuilder<?>> getBuilder(Class<?> generatedType) {
        IGeneratorBuilder<?> matchedBuilder = null;
        for (GenBuilderInfo info : getBasicBuilderInfoList()) {
            if (info.getRuleInfo().getGeneratedType() == generatedType || info.getRuleInfo().getGeneratedType().isAssignableFrom(generatedType)) {
                if (matchedBuilder != null) {
                    throw new DtoGeneratorException("More than one matched generators found for generated type: " +
                            "'" + generatedType + "'");
                }
                matchedBuilder = info.getBuilderSupplier().get();
            }
        }
        return matchedBuilder != null ?
                Optional.of(matchedBuilder) :
                Optional.empty();
    }

    public boolean isBuilderExist(Class<?> generatedType, Annotation rule) {
        return getOtherRulesInfoList().stream().anyMatch(i ->
                i.rules == rule.annotationType() &&
                        (i.getGeneratedType() == generatedType || i.getGeneratedType().isAssignableFrom(generatedType)))
                ||
                getBasicBuilderInfoList().stream().anyMatch(i ->
                        i.getRuleInfo().getRules() == rule.annotationType() &&
                                (i.getRuleInfo().getGeneratedType() == generatedType ||
                                        i.getRuleInfo().getGeneratedType().isAssignableFrom(generatedType)));
    }

    public static StringGenerator.StringGeneratorBuilder stringBuilder() {
        return StringGenerator.builder();
    }

    public static DoubleGenerator.DoubleGeneratorBuilder doubleBuilder() {
        return DoubleGenerator.builder();
    }

    public static IntegerGenerator.IntegerGeneratorBuilder integerBuilder() {
        return IntegerGenerator.builder();
    }

    public static LongGenerator.LongGeneratorBuilder longBuilder() {
        return LongGenerator.builder();
    }

    public static EnumGenerator.EnumGeneratorBuilder enumBuilder() {
        return EnumGenerator.builder();
    }

    public static CollectionGenerator.CollectionGeneratorBuilder<?> collectionBuilder() {
        return CollectionGenerator.builder();
    }

    public static LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder localDateTimeBuilder() {
        return LocalDateTimeGenerator.builder();
    }

    @Getter
    private static class GenBuilderInfo {
        private static GenBuilderInfo createInstance(Class<? extends Annotation> rules,
                                                     Class<?> generatedType,
                                                     Supplier<IGeneratorBuilder<?>> builderSupplier) {
            GenBuilderInfo genBuilderInfo = new GenBuilderInfo();
            genBuilderInfo.ruleInfo = RuleInfo.createInstance(rules, generatedType);
            genBuilderInfo.builderSupplier = builderSupplier;
            return genBuilderInfo;
        }

        RuleInfo ruleInfo;
        Supplier<IGeneratorBuilder<?>> builderSupplier;
    }

    @Value(staticConstructor = "createInstance")
    private static class RuleInfo {
        Class<? extends Annotation> rules;
        Class<?> generatedType;
    }
}
