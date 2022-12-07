package org.laoruga.dtogenerator.typegenerators.builders;

import org.laoruga.dtogenerator.typegenerators.*;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
public class GeneratorBuildersFactory {
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

    public static CollectionGenerator.CollectionGeneratorBuilder<?> setBuilder() {
        return CollectionGenerator.builder();
    }

    public static CollectionGenerator.CollectionGeneratorBuilder<?> listBuilder() {
        return CollectionGenerator.builder();
    }

    public static LocalDateTimeGenerator.LocalDateTimeGeneratorBuilder localDateTimeBuilder() {
        return LocalDateTimeGenerator.builder();
    }

    public static CustomGenerator.CustomGeneratorBuilder customBuilder() {
        return CustomGenerator.builder();
    }

    public static NestedDtoGenerator.NestedDtoGeneratorBuilder nestedDtoBuilder() {
        return NestedDtoGenerator.builder();
    }
}
