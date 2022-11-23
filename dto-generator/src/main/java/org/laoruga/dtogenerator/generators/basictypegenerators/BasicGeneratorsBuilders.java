package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 13.05.2022
 */
public final class BasicGeneratorsBuilders {

    private static BasicGeneratorsBuilders instance;

    @Getter(AccessLevel.PRIVATE)
    private List<GenBuilderInfo> genBuilderInfoList;

    private BasicGeneratorsBuilders() {
        genBuilderInfoList = Arrays.asList(
                GenBuilderInfo.createInstance(StringRule.class, String.class, BasicGeneratorsBuilders::stringBuilder),
                GenBuilderInfo.createInstance(IntegerRule.class, Integer.class, BasicGeneratorsBuilders::integerBuilder),
                GenBuilderInfo.createInstance(IntegerRule.class, Integer.TYPE, BasicGeneratorsBuilders::integerBuilder),
                GenBuilderInfo.createInstance(DoubleRule.class, Double.class, BasicGeneratorsBuilders::doubleBuilder),
                GenBuilderInfo.createInstance(DoubleRule.class, Double.TYPE, BasicGeneratorsBuilders::doubleBuilder),
                GenBuilderInfo.createInstance(LongRule.class, Long.class, BasicGeneratorsBuilders::longBuilder),
                GenBuilderInfo.createInstance(LongRule.class, Long.TYPE, BasicGeneratorsBuilders::longBuilder),
                GenBuilderInfo.createInstance(EnumRule.class, Enum.class, BasicGeneratorsBuilders::enumBuilder),
                GenBuilderInfo.createInstance(LocalDateTimeRule.class, LocalDateTime.class, BasicGeneratorsBuilders::longBuilder),

                GenBuilderInfo.createInstance(SetRule.class, Collection.class, BasicGeneratorsBuilders::collectionBuilder),
                GenBuilderInfo.createInstance(ListRule.class, Collection.class, BasicGeneratorsBuilders::collectionBuilder),

                GenBuilderInfo.createInstance(NestedDtoRules.class, Object.class, null),
                GenBuilderInfo.createInstance(CustomRule.class, Object.class, null)

        );
    }

    @Value(staticConstructor = "createInstance")
    private static class GenBuilderInfo {
        Class<? extends Annotation> rules;
        Class<?> generatedType;
        Supplier<IGeneratorBuilder<?>> builderSupplier;
    }

    public static synchronized BasicGeneratorsBuilders getInstance() {
        if (instance == null) {
            instance = new BasicGeneratorsBuilders();
        }
        return instance;
    }

    public Optional<IGeneratorBuilder<?>> getBuilder(Class<?> generatedType) {
        IGeneratorBuilder<?> matchedBuilder = null;
        for (GenBuilderInfo info : getGenBuilderInfoList()) {
            if (info.getGeneratedType() == generatedType || info.getGeneratedType().isAssignableFrom(generatedType)) {
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
        return getGenBuilderInfoList().stream().anyMatch(i ->
                i.rules == rule.annotationType() &&
                (i.getGeneratedType() == generatedType || i.getGeneratedType().isAssignableFrom(generatedType)));
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
}
