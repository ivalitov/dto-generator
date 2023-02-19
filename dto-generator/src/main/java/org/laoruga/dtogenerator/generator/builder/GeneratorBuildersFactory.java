package org.laoruga.dtogenerator.generator.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.generator.*;
import org.laoruga.dtogenerator.generator.builder.builders.*;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeneratorBuildersFactory {
    public static StringGeneratorBuilder stringBuilder() {
        return StringGenerator.builder();
    }

    public static DoubleGeneratorBuilder doubleBuilder() {
        return DoubleGenerator.builder();
    }

    public static IntegerGeneratorBuilder integerBuilder() {
        return IntegerGenerator.builder();
    }

    public static LongGeneratorBuilder longBuilder() {
        return LongGenerator.builder();
    }

    public static EnumGeneratorBuilder enumBuilder() {
        return EnumGenerator.builder();
    }

    public static CollectionGeneratorBuilder<?> setBuilder() {
        return CollectionGenerator.builder();
    }

    public static CollectionGeneratorBuilder<?> listBuilder() {
        return CollectionGenerator.builder();
    }

    public static LocalDateTimeGeneratorBuilder localDateTimeBuilder() {
        return LocalDateTimeGenerator.builder();
    }

    public static CustomGeneratorBuilder customBuilder() {
        return CustomGenerator.builder();
    }

    public static NestedDtoGeneratorBuilder nestedDtoBuilder() {
        return NestedDtoGenerator.builder();
    }
}
