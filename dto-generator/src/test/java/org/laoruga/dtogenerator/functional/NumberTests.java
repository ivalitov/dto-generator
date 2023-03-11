package org.laoruga.dtogenerator.functional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.rules.NumberRule;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.generator.configs.NumberCommonConfigDto;
import org.laoruga.dtogenerator.generator.configs.NumberConfigDto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.RuleRemark.*;

/**
 * @author Il'dar Valitov
 * Created on 27.02.2023
 */
@ExtendWith(Extensions.RestoreStaticConfig.class)
public class NumberTests {

    static class Dto {

        @NumberRule
        Integer intObject;

        @NumberRule(minInt = 1)
        int intPrimitive;

        @NumberRule
        Long longObject;

        @NumberRule(maxLong = 5L)
        long longPrimitive;

        @NumberRule
        Short shortObject;

        @NumberRule(minShort = -5, maxShort = -5)
        short shortPrimitive;

        @NumberRule
        Byte byteObject;

        @NumberRule(maxByte = 10)
        byte bytePrimitive;

    }

    @Test
    public void annotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, notNullValue()),
                () -> assertThat(dto.intPrimitive, greaterThanOrEqualTo(1)),
                () -> assertThat(dto.longObject, notNullValue()),
                () -> assertThat(dto.longPrimitive, lessThanOrEqualTo(5L)),
                () -> assertThat(dto.shortObject, notNullValue()),
                () -> assertThat(dto.shortPrimitive, equalTo((short) -5)),
                () -> assertThat(dto.byteObject, notNullValue()),
                () -> assertThat(dto.bytePrimitive, lessThanOrEqualTo((byte) 10))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    public void staticConfig() {

        NumberCommonConfigDto numberConfig = DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig().getNumberConfig();
        numberConfig.setMinIntValue(-99);
        numberConfig.setMaxIntValue(0);
        numberConfig.setMinLongValue(9_999_999_999L);
        numberConfig.setMaxLongValue(99_999_999_999L);
        numberConfig.setMaxShortValue((short) 100);
        numberConfig.setMinShortValue(new Short("77"));
        numberConfig.setMinByteValue((byte) 126);
        numberConfig.setMaxByteValue(Byte.MAX_VALUE);
        numberConfig.setRuleRemark(MIN_VALUE);

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(-99)),
                () -> assertThat(dto.intPrimitive, equalTo(-99)),
                () -> assertThat(dto.longObject, equalTo(9_999_999_999L)),
                () -> assertThat(dto.longPrimitive, equalTo(9_999_999_999L)),
                () -> assertThat(dto.shortObject, equalTo((short) 77)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 77)),
                () -> assertThat(dto.byteObject, equalTo((byte) 126)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 126))
        );

    }

    @Test
    public void instanceConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);
        NumberCommonConfigDto numberConfig = builder.getTypeGeneratorConfig().getNumberConfig();

        numberConfig.setMinIntValue(-99);
        numberConfig.setMaxIntValue(0);
        numberConfig.setMinLongValue(new Long(9_999_999_999L));
        numberConfig.setMaxLongValue(99_999_999_999L);
        numberConfig.setMaxShortValue((short) 100);
        numberConfig.setMinShortValue((short) 77);
        numberConfig.setMinByteValue((byte) 126);
        numberConfig.setMaxByteValue(Byte.MAX_VALUE);
        numberConfig.setRuleRemark(MAX_VALUE);


        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(0)),
                () -> assertThat(dto.intPrimitive, equalTo(0)),
                () -> assertThat(dto.longObject, equalTo(99_999_999_999L)),
                () -> assertThat(dto.longPrimitive, equalTo(99_999_999_999L)),
                () -> assertThat(dto.shortObject, equalTo((short) 100)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 100)),
                () -> assertThat(dto.byteObject, equalTo(Byte.MAX_VALUE)),
                () -> assertThat(dto.bytePrimitive, equalTo(Byte.MAX_VALUE))
        );

    }

    @Test
    public void fieldConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder
                .setTypeGeneratorConfig("intObject",
                        NumberConfigDto.builder().minValue(1).maxValue(101).ruleRemark(MAX_VALUE).build())
                .setTypeGeneratorConfig("intPrimitive",
                        NumberConfigDto.builder().minValue(-1).maxValue(-1).ruleRemark(RANDOM_VALUE).build())
                .setTypeGeneratorConfig("longObject",
                        NumberConfigDto.builder().minValue(11_999_999_999L).maxValue(111_999_999_999L).ruleRemark(MAX_VALUE).build())
                .setTypeGeneratorConfig("longPrimitive",
                        NumberConfigDto.builder().minValue(0L).maxValue(0L).ruleRemark(RANDOM_VALUE).build())
                .setTypeGeneratorConfig("shortObject",
                        NumberConfigDto.builder().minValue((short) 111).maxValue(new Short("111")).ruleRemark(RANDOM_VALUE).build())
                .setTypeGeneratorConfig("shortPrimitive",
                        NumberConfigDto.builder().minValue(new Short("0")).maxValue((short) 0).ruleRemark(MIN_VALUE).build())
                .setTypeGeneratorConfig("byteObject",
                        NumberConfigDto.builder().minValue(new Byte("-12")).maxValue((byte) -11).ruleRemark(MIN_VALUE).build())
                .setTypeGeneratorConfig("bytePrimitive",
                        NumberConfigDto.builder().minValue((byte) 11).maxValue(new Byte("12")).ruleRemark(MAX_VALUE).build());


        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(101)),
                () -> assertThat(dto.intPrimitive, equalTo(-1)),
                () -> assertThat(dto.longObject, equalTo(111_999_999_999L)),
                () -> assertThat(dto.longPrimitive, equalTo(0L)),
                () -> assertThat(dto.shortObject, equalTo((short) 111)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 0)),
                () -> assertThat(dto.byteObject, equalTo((byte) -12)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 12))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    public void staticAndInstanceAndFieldAndAnnotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        // static
        NumberCommonConfigDto numberConfigStatic = DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig().getNumberConfig();
        numberConfigStatic.setMaxIntValue(100);
        numberConfigStatic.setMinLongValue(-321L);
        numberConfigStatic.setMinShortValue((short) -111);
        numberConfigStatic.setRuleRemark(MIN_VALUE);

        // instance
        NumberCommonConfigDto numberConfigInstance = builder.getTypeGeneratorConfig().getNumberConfig();
        numberConfigInstance.setMaxLongValue(321L);
        numberConfigInstance.setMinShortValue((short) -222);
        numberConfigInstance.setMaxShortValue((short) 222);

        // field
        builder.setTypeGeneratorConfig("intObject", NumberConfigDto.builder().ruleRemark(MIN_VALUE).build());
        builder.setTypeGeneratorConfig("intPrimitive", NumberConfigDto.builder().ruleRemark(MAX_VALUE).build());
        builder.setTypeGeneratorConfig("longObject", NumberConfigDto.builder().ruleRemark(MAX_VALUE).build());
        builder.setTypeGeneratorConfig("longPrimitive", NumberConfigDto.builder().ruleRemark(MIN_VALUE).build());
        builder.setTypeGeneratorConfig("shortObject", NumberConfigDto.builder().ruleRemark(MAX_VALUE).build());
        builder.setTypeGeneratorConfig("shortPrimitive", NumberConfigDto.builder().minValue((short) -100).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Annotation + field", dto.intObject, equalTo(Integer.MIN_VALUE)),
                () -> assertThat("Static + annotation + field", dto.intPrimitive, equalTo(100)),
                () -> assertThat("Instance + field", dto.longObject, equalTo(321L)),
                () -> assertThat("Static + field", dto.longPrimitive, equalTo(-321L)),
                () -> assertThat("Static + instance + field", dto.shortObject, equalTo((short) 222)),
                () -> assertThat("Static + field", dto.shortPrimitive, equalTo((short) -100)),
                () -> assertThat("Static + field", dto.byteObject, equalTo(Byte.MIN_VALUE)),
                () -> assertThat("Static + field", dto.bytePrimitive, equalTo(Byte.MIN_VALUE))
        );

    }

    @Test
    public void overrideGeneratorByField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator("intObject", () -> new Integer("1"));
        builder.setGenerator("intPrimitive", () -> 2);
        builder.setGenerator("longObject", () -> 3L);
        builder.setGenerator("longPrimitive", () -> new Long("4"));
        builder.setGenerator("shortObject", () -> new Short("5"));
        builder.setGenerator("shortPrimitive", () -> (short) 6);
        builder.setGenerator("byteObject", () -> (byte) 7);
        builder.setGenerator("bytePrimitive", () -> (byte) 8);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(1)),
                () -> assertThat(dto.intPrimitive, equalTo(2)),
                () -> assertThat(dto.longObject, equalTo(3L)),
                () -> assertThat(dto.longPrimitive, equalTo(4L)),
                () -> assertThat(dto.shortObject, equalTo((short) 5)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 6)),
                () -> assertThat(dto.byteObject, equalTo((byte) 7)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 8))
        );

    }

    @Test
    public void overrideGeneratorByType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator(Integer.class, () -> 1);
        builder.setGenerator(Long.class, () -> new Long("2"));
        builder.setGenerator(Short.class, () -> (short) 3);
        builder.setGenerator(Byte.class, () -> new Byte("4"));

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(1)),
                () -> assertThat(dto.intPrimitive, equalTo(1)),
                () -> assertThat(dto.longObject, equalTo(2L)),
                () -> assertThat(dto.longPrimitive, equalTo(2L)),
                () -> assertThat(dto.shortObject, equalTo((short) 3)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 3)),
                () -> assertThat(dto.byteObject, equalTo((byte) 4)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 4))
        );

    }

    @Test
    public void overrideGeneratorByTypeAndField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator(Integer.class, () -> 1);
        builder.setGenerator(Long.class, () -> new Long("2"));
        builder.setGenerator(Short.class, () -> (short) 3);
        builder.setGenerator(Byte.class, () -> new Byte("4"));

        builder.setGenerator("intPrimitive", () -> 11);
        builder.setGenerator("longObject", () -> 22L);
        builder.setGenerator("shortPrimitive", () -> (short) 33);
        builder.setGenerator("byteObject", () -> (byte) 44);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(1)),
                () -> assertThat(dto.intPrimitive, equalTo(11)),
                () -> assertThat(dto.longObject, equalTo(22L)),
                () -> assertThat(dto.longPrimitive, equalTo(2L)),
                () -> assertThat(dto.shortObject, equalTo((short) 3)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 33)),
                () -> assertThat(dto.byteObject, equalTo((byte) 44)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 4))
        );

    }

    static class Dto_2 {

        Integer intObject;
        int intPrimitive;
        Long longObject;
        long longPrimitive;
        Short shortObject;
        short shortPrimitive;
        Byte byteObject;
        byte bytePrimitive;

    }

    @Test
    public void withoutAnnotations() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        builder.getConfig().setGenerateAllKnownTypes(true);
        builder.getTypeGeneratorConfig().getNumberConfig().setRuleRemark(MAX_VALUE);

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(Integer.MAX_VALUE)),
                () -> assertThat(dto.intPrimitive, equalTo(Integer.MAX_VALUE)),
                () -> assertThat(dto.longObject, equalTo(Long.MAX_VALUE)),
                () -> assertThat(dto.longPrimitive, equalTo(Long.MAX_VALUE)),
                () -> assertThat(dto.shortObject, equalTo(Short.MAX_VALUE)),
                () -> assertThat(dto.shortPrimitive, equalTo(Short.MAX_VALUE)),
                () -> assertThat(dto.byteObject, equalTo(Byte.MAX_VALUE)),
                () -> assertThat(dto.bytePrimitive, equalTo(Byte.MAX_VALUE))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    public void withoutAnnotationsWithOverriddenConfig() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig().getNumberConfig().setRuleRemark(MAX_VALUE);
        builder.getConfig().setGenerateAllKnownTypes(true);

        NumberCommonConfigDto numberConfig = builder.getTypeGeneratorConfig().getNumberConfig();

        // next line overrides MAX_VALUE from static config
        numberConfig.setRuleRemark(MIN_VALUE);
        numberConfig.setMinIntValue(1);
        numberConfig.setMinLongValue(2L);
        numberConfig.setMaxLongValue(22_222_222_222L);
        numberConfig.setMinShortValue((short) 3);
        numberConfig.setMinByteValue((byte) 4);

        // next lines override parts of previous configs
        builder.setTypeGeneratorConfig("intPrimitive", NumberConfigDto.builder().minValue(-1).build());
        builder.setTypeGeneratorConfig("longObject", NumberConfigDto.builder().ruleRemark(MAX_VALUE).build());
        builder.setTypeGeneratorConfig("shortPrimitive", NumberConfigDto.builder().minValue((short) -3).build());
        builder.setTypeGeneratorConfig("byteObject", NumberConfigDto.builder().minValue(new Byte("-4")).build());

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(1)),
                () -> assertThat(dto.intPrimitive, equalTo(-1)),
                () -> assertThat(dto.longObject, equalTo(22_222_222_222L)),
                () -> assertThat(dto.longPrimitive, equalTo(2L)),
                () -> assertThat(dto.shortObject, equalTo((short) 3)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) -3)),
                () -> assertThat(dto.byteObject, equalTo((byte) -4)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 4))
        );

    }

}
