package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.rules.DecimalRule;
import org.laoruga.dtogenerator.constants.Bounds;
import org.laoruga.dtogenerator.generator.config.dto.DecimalConfig;

import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.Boundary.*;

/**
 * @author Il'dar Valitov
 * Created on 13.03.2023
 */

@ExtendWith(Extensions.RestoreStaticConfig.class)
@Epic("DECIMAL_RULES")
public class DecimalTests {

    static class Dto {

        @DecimalRule
        Double doubleObject;

        @DecimalRule(minDouble = -5)
        double doublePrimitive;

        @DecimalRule
        Float floatObject;

        @DecimalRule(maxFloat = 123F)
        float floatPrimitive;

        @DecimalRule
        BigDecimal bigDecimal;

    }

    @Test
    void annotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, notNullValue()),
                () -> assertThat(dto.doublePrimitive, greaterThanOrEqualTo(-5D)),
                () -> assertThat(dto.floatObject, notNullValue()),
                () -> assertThat(dto.floatPrimitive, lessThanOrEqualTo(123F)),
                () -> assertThat(dto.bigDecimal, notNullValue())
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.getStaticConfig().getTypeGeneratorsConfig().getDecimalConfig()
                .setMinDoubleValue(1)
                .setMaxDoubleValue(2)
                .setMinFloatValue(3.3F)
                .setMaxFloatValue(new Float(5))
                .setRuleRemarkFloat(MAX_VALUE)
                .setMinBigDecimalValue("6")
                .setMaxBigDecimalValue(new BigDecimal("7"))
                .setRuleRemark(MIN_VALUE);


        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, equalTo(1D)),
                () -> assertThat(dto.doublePrimitive, equalTo(1D)),
                () -> assertThat(dto.floatObject, equalTo(5F)),
                () -> assertThat(dto.floatPrimitive, equalTo(5F)),
                () -> assertThat(dto.bigDecimal, equalTo(new BigDecimal("6")))
        );
    }

    @Test
    void instanceConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);
        builder.getConfig().getTypeGeneratorsConfig().getDecimalConfig()
                .setMinDoubleValue(1)
                .setMaxDoubleValue(2)
                .setMinFloatValue(3.3F)
                .setMaxFloatValue(4F)
                .setMinBigDecimalValue("6")
                .setMaxBigDecimalValue(new BigDecimal("7"))
                .setRuleRemarkBigDecimal(MIN_VALUE)
                .setRuleRemark(MAX_VALUE);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, equalTo(2D)),
                () -> assertThat(dto.doublePrimitive, equalTo(2D)),
                () -> assertThat(dto.floatObject, equalTo(4F)),
                () -> assertThat(dto.floatPrimitive, equalTo(4F)),
                () -> assertThat(dto.bigDecimal, equalTo(new BigDecimal("6")))
        );
    }

    @Test
    void fieldConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder
                .setGeneratorConfig("doubleObject",
                        DecimalConfig.builder()
                                .minValue(1.0)
                                .maxValue(101.101)
                                .ruleRemark(MAX_VALUE).build())

                .setGeneratorConfig("doublePrimitive",
                        DecimalConfig.builder()
                                .minValue(-1D)
                                .maxValue(-1D)
                                .ruleRemark(RANDOM_VALUE).build())

                .setGeneratorConfig("floatObject",
                        DecimalConfig.builder()
                                .minValue(11_999_999_999F)
                                .maxValue(111_999_999_999F)
                                .ruleRemark(MAX_VALUE).build())

                .setGeneratorConfig("floatPrimitive",
                        DecimalConfig.builder()
                                .minValue(0F)
                                .maxValue(0F)
                                .ruleRemark(MIN_VALUE).build())

                .setGeneratorConfig("bigDecimal",
                        DecimalConfig.builder()
                                .minValue(new BigDecimal("111"))
                                .maxValue(new BigDecimal("111"))
                                .ruleRemark(RANDOM_VALUE).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, equalTo(101.101)),
                () -> assertThat(dto.doublePrimitive, equalTo(-1D)),
                () -> assertThat(dto.floatObject, equalTo(111_999_999_999F)),
                () -> assertThat(dto.floatPrimitive, equalTo(0F)),
                () -> assertThat(dto.bigDecimal, equalTo(new BigDecimal("111")))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticAndInstanceAndFieldAndAnnotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        // static
        builder.getStaticConfig().getTypeGeneratorsConfig().getDecimalConfig()
                .setMaxDoubleValue(100D)
                .setMinFloatValue(-321F)
                .setMinBigDecimalValue((new BigDecimal(-111)))
                .setRuleRemark(MIN_VALUE);

        // instance
        builder.getConfig().getTypeGeneratorsConfig().getDecimalConfig()
                .setMaxFloatValue(321F)
                .setMinBigDecimalValue((new BigDecimal(-222)));

        // field
        builder.setGeneratorConfig("doubleObject", DecimalConfig.builder().ruleRemark(MIN_VALUE).build())
                .setGeneratorConfig("doublePrimitive", DecimalConfig.builder().ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("floatObject", DecimalConfig.builder().ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("floatPrimitive", DecimalConfig.builder().ruleRemark(MIN_VALUE).build())
                .setGeneratorConfig("bigDecimal", DecimalConfig.builder().ruleRemark(MIN_VALUE).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Annotation + field", dto.doubleObject, equalTo(Bounds.DOUBLE_MIN_VALUE)),
                () -> assertThat("Static + field", dto.doublePrimitive, equalTo(100D)),
                () -> assertThat("Instance + field", dto.floatObject, equalTo(321F)),
                () -> assertThat("Static + field", dto.floatPrimitive, equalTo(-321F)),
                () -> assertThat("Static + instance", dto.bigDecimal, equalTo(new BigDecimal(-222)))
        );
    }

    @Test
    void overrideGeneratorByField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder
                .setGenerator("doubleObject", () -> new Double("1"))
                .setGenerator("doublePrimitive", () -> 2D)
                .setGenerator("floatObject", () -> 3F)
                .setGenerator("floatPrimitive", () -> new Float("4"))
                .setGenerator("bigDecimal", () -> new BigDecimal("5"));

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, equalTo(1D)),
                () -> assertThat(dto.doublePrimitive, equalTo(2D)),
                () -> assertThat(dto.floatObject, equalTo(3F)),
                () -> assertThat(dto.floatPrimitive, equalTo(4F)),
                () -> assertThat(dto.bigDecimal, equalTo(new BigDecimal("5")))
        );
    }

    @Test
    void overrideGeneratorByType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator(Double.class, () -> 1D)
                .setGenerator(Float.class, () -> new Float(2))
                .setGenerator(BigDecimal.class, () -> new BigDecimal(3));

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, equalTo(1D)),
                () -> assertThat(dto.doublePrimitive, equalTo(1D)),
                () -> assertThat(dto.floatObject, equalTo(2F)),
                () -> assertThat(dto.floatPrimitive, equalTo(2F)),
                () -> assertThat(dto.bigDecimal, equalTo(new BigDecimal("3")))
        );
    }

    static class Dto_2 {

        Double doubleObject;
        double doublePrimitive;
        Float floatObject;
        float floatPrimitive;
        BigDecimal bigDecimal;
    }

    @Test
    void withoutAnnotations() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);
        builder.getConfig().getTypeGeneratorsConfig().getDecimalConfig().setRuleRemark(MAX_VALUE);

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, equalTo(Bounds.DOUBLE_MAX_VALUE)),
                () -> assertThat(dto.doublePrimitive, equalTo(Bounds.DOUBLE_MAX_VALUE)),
                () -> assertThat(dto.floatObject, equalTo(Bounds.FLOAT_MAX_VALUE)),
                () -> assertThat(dto.floatPrimitive, equalTo(Bounds.FLOAT_MAX_VALUE)),
                () -> assertThat(dto.bigDecimal, equalTo(new BigDecimal(Bounds.BIG_DECIMAL_MAX_VALUE)))
        );
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void withoutAnnotationsWithOverriddenConfig() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        builder.getStaticConfig().getTypeGeneratorsConfig().getDecimalConfig().setRuleRemark(MAX_VALUE);
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);


        builder.getConfig().getTypeGeneratorsConfig().getDecimalConfig()
                // next line overrides MAX_VALUE from static config
                .setRuleRemark(MIN_VALUE)
                .setMinDoubleValue(1D)
                .setMaxDoubleValue(1D)
                .setMinFloatValue(2F)
                .setMaxFloatValue(2F)
                .setMinBigDecimalValue("22222222222");

        // next lines override parts of previous configs
        builder.setGeneratorConfig("doubleObject", DecimalConfig.builder().minValue(-1D).build())
                .setGeneratorConfig("doublePrimitive", DecimalConfig.builder().ruleRemark(MAX_VALUE).maxValue(2D).build())
                .setGeneratorConfig("floatObject", DecimalConfig.builder().minValue(-3F).build())
                .setGeneratorConfig("floatPrimitive", DecimalConfig.builder().minValue(new Float("-4")).build())
                .setGeneratorConfig("bigDecimal", DecimalConfig.builder().minValue(new BigDecimal("-5")).build());

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.doubleObject, equalTo(-1D)),
                () -> assertThat(dto.doublePrimitive, equalTo(2D)),
                () -> assertThat(dto.floatObject, equalTo(-3F)),
                () -> assertThat(dto.floatPrimitive, equalTo(-4F)),
                () -> assertThat(dto.bigDecimal, equalTo(new BigDecimal("-5")))
        );
    }

    static class Dto_3 {

        @DecimalRule(minDouble = 10, maxDouble = 10.99999999999, precision = 3)
        double threeSymbols;

    }

    @Test
    void precisionTest() {

        DtoGeneratorBuilder<Dto_3> builder = DtoGenerator.builder(Dto_3.class);
        DtoGenerator<Dto_3> generator = builder.build();

        Pattern pattern = Pattern.compile("10[.,][0-9]{3}\\b");

        long count = IntStream.range(1, 100).boxed()
                .map(i -> String.valueOf(generator.generateDto().threeSymbols))
                .filter(i -> pattern.matcher(i).matches())
                .count();

        assertAll(
                () -> assertThat(count, greaterThan(30L))
        );
    }

    @Test
    void precisionTestOverriddenConfigViaBuilder() {

        DtoGeneratorBuilder<Dto_3> builder = DtoGenerator.builder(Dto_3.class);
        builder.setGeneratorConfig(Double.class,
                DecimalConfig.builder()
                        .minValue(11D)
                        .maxValue(11.9999999D)
                        .precision(5).build());

        DtoGenerator<Dto_3> generator = builder.build();

        Pattern pattern = Pattern.compile("11[.,][0-9]{5}\\b");

        long count = IntStream.range(1, 100).boxed()
                .map(i -> String.valueOf(generator.generateDto().threeSymbols))
                .filter(i -> pattern.matcher(i).matches())
                .count();

        assertAll(
                () -> assertThat(count, greaterThan(30L))
        );
    }

    @Test
    void precisionTestOverriddenConfigViaTypeConfigs() {
        DtoGeneratorBuilder<Dto_3> builder = DtoGenerator.builder(Dto_3.class);

        builder.getConfig().getTypeGeneratorsConfig().getDecimalConfig()
                .setMaxDoubleValue(12D)
                .setMinDoubleValue(12.99999999D)
                .setPrecisionDouble(4);

        DtoGenerator<Dto_3> generator = builder.build();

        Pattern pattern = Pattern.compile("12[.,][0-9]{4}\\b");

        long count = IntStream.range(1, 100).boxed()
                .map(i -> String.valueOf(generator.generateDto().threeSymbols))
                .filter(i -> pattern.matcher(i).matches())
                .count();

        assertAll(
                () -> assertThat(count, greaterThan(30L))
        );
    }

}
