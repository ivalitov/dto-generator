package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.rules.IntegralRule;
import org.laoruga.dtogenerator.constants.Bounds;
import org.laoruga.dtogenerator.generator.config.dto.IntegralConfig;
import org.laoruga.dtogenerator.generator.config.dto.IntegralConfigCommonConfig;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.Boundary.*;

/**
 * @author Il'dar Valitov
 * Created on 27.02.2023
 */
@ExtendWith(Extensions.RestoreStaticConfig.class)
@Epic("NUMBER_RULES")
public class NumberTests {

    static class Dto {

        @IntegralRule
        Integer intObject;

        @IntegralRule(minInt = 1)
        int intPrimitive;

        @IntegralRule
        Long longObject;

        @IntegralRule(maxLong = 5L)
        long longPrimitive;

        @IntegralRule
        Short shortObject;

        @IntegralRule(minShort = -5, maxShort = -5)
        short shortPrimitive;

        @IntegralRule
        Byte byteObject;

        @IntegralRule(maxByte = 10)
        byte bytePrimitive;

        @IntegralRule(minInt = 1, maxInt = 1)
        AtomicInteger atomicInteger;

        @IntegralRule(minLong = 2, maxLong = 2)
        AtomicLong atomicLong;

        @IntegralRule(minBigInt = "9999999999999999999", maxBigInt = "9999999999999999999")
        BigInteger bigInteger;

    }

    @Test
    void annotationConfig() {

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
                () -> assertThat(dto.bytePrimitive, lessThanOrEqualTo((byte) 10)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(1)),
                () -> assertThat(dto.atomicLong.get(), equalTo(2L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("9999999999999999999")))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.getStaticConfig().getTypeGeneratorsConfig().getIntegralConfig()
                .setMinIntValue(-99)
                .setMaxIntValue(0)
                .setMinLongValue(9_999_999_999L)
                .setMaxLongValue(99_999_999_999L)
                .setRuleRemarkLong(MAX_VALUE)
                .setMaxShortValue((short) 100)
                .setMinShortValue(new Short("77"))
                .setMinByteValue((byte) 126)
                .setMaxBigIntValue("888888")
                .setMinBigIntValue("-9999999999999999999")
                .setMaxByteValue(Byte.MAX_VALUE)
                .setRuleRemark(MIN_VALUE);


        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(-99)),
                () -> assertThat(dto.intPrimitive, equalTo(-99)),
                () -> assertThat(dto.longObject, equalTo(99_999_999_999L)),
                () -> assertThat(dto.longPrimitive, equalTo(99_999_999_999L)),
                () -> assertThat(dto.shortObject, equalTo((short) 77)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 77)),
                () -> assertThat(dto.byteObject, equalTo((byte) 126)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 126)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(-99)),
                () -> assertThat(dto.atomicLong.get(), equalTo(99_999_999_999L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("-9999999999999999999")))

        );

    }

    @Test
    void instanceConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);
        builder.getConfig().getTypeGeneratorsConfig().getIntegralConfig()
                .setMinIntValue(-99)
                .setMaxIntValue(0)
                .setRuleRemarkInt(MIN_VALUE)
                .setMinLongValue(new Long(9_999_999_999L))
                .setMaxLongValue(99_999_999_999L)
                .setMaxShortValue((short) 100)
                .setMinShortValue((short) 77)
                .setMinByteValue((byte) 126)
                .setMaxBigIntValue(new BigInteger("911"))
                .setMaxByteValue(Byte.MAX_VALUE)
                .setRuleRemark(MAX_VALUE);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(-99)),
                () -> assertThat(dto.intPrimitive, equalTo(-99)),
                () -> assertThat(dto.longObject, equalTo(99_999_999_999L)),
                () -> assertThat(dto.longPrimitive, equalTo(99_999_999_999L)),
                () -> assertThat(dto.shortObject, equalTo((short) 100)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 100)),
                () -> assertThat(dto.byteObject, equalTo(Byte.MAX_VALUE)),
                () -> assertThat(dto.bytePrimitive, equalTo(Byte.MAX_VALUE)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(-99)),
                () -> assertThat(dto.atomicLong.get(), equalTo(99_999_999_999L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("911")))
        );

    }

    @Test
    void fieldConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder
                .setGeneratorConfig("intObject",
                        IntegralConfig.builder().minValue(1).maxValue(101).ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("intPrimitive",
                        IntegralConfig.builder().minValue(-1).maxValue(-1).ruleRemark(RANDOM_VALUE).build())
                .setGeneratorConfig("longObject",
                        IntegralConfig.builder().minValue(11_999_999_999L).maxValue(111_999_999_999L).ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("longPrimitive",
                        IntegralConfig.builder().minValue(0L).maxValue(0L).ruleRemark(RANDOM_VALUE).build())
                .setGeneratorConfig("shortObject",
                        IntegralConfig.builder().minValue((short) 111).maxValue(new Short("111")).ruleRemark(RANDOM_VALUE).build())
                .setGeneratorConfig("shortPrimitive",
                        IntegralConfig.builder().minValue(new Short("0")).maxValue((short) 0).ruleRemark(MIN_VALUE).build())
                .setGeneratorConfig("byteObject",
                        IntegralConfig.builder().minValue(new Byte("-12")).maxValue((byte) -11).ruleRemark(MIN_VALUE).build())
                .setGeneratorConfig("bytePrimitive",
                        IntegralConfig.builder().minValue((byte) 11).maxValue(new Byte("12")).ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("atomicInteger",
                        IntegralConfig.builder().minValue(2).maxValue(new Integer("2")).ruleRemark(RANDOM_VALUE).build())
                .setGeneratorConfig("atomicLong",
                        IntegralConfig.builder().minValue(3L).maxValue(new Long("3")).ruleRemark(RANDOM_VALUE).build())
                .setGeneratorConfig("bigInteger",
                        IntegralConfig.builder().minValue(new BigInteger("0")).maxValue(new BigInteger("0")).ruleRemark(MIN_VALUE).build());


        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(101)),
                () -> assertThat(dto.intPrimitive, equalTo(-1)),
                () -> assertThat(dto.longObject, equalTo(111_999_999_999L)),
                () -> assertThat(dto.longPrimitive, equalTo(0L)),
                () -> assertThat(dto.shortObject, equalTo((short) 111)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 0)),
                () -> assertThat(dto.byteObject, equalTo((byte) -12)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 12)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(2)),
                () -> assertThat(dto.atomicLong.get(), equalTo(3L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("0")))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticAndInstanceAndFieldAndAnnotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        // static
        IntegralConfigCommonConfig numberConfigStatic = builder.getStaticConfig().getTypeGeneratorsConfig().getIntegralConfig();
        numberConfigStatic.setMaxIntValue(100);
        numberConfigStatic.setMinLongValue(-321L);
        numberConfigStatic.setMinShortValue((short) -111);
        numberConfigStatic.setRuleRemark(MIN_VALUE);

        // instance
        IntegralConfigCommonConfig numberConfigInstance = builder.getConfig().getTypeGeneratorsConfig().getIntegralConfig();
        numberConfigInstance.setMaxLongValue(321L);
        numberConfigInstance.setMinShortValue((short) -222);
        numberConfigInstance.setMaxShortValue((short) 222);

        // field
        builder.setGeneratorConfig("intObject", IntegralConfig.builder().ruleRemark(MIN_VALUE).build());
        builder.setGeneratorConfig("intPrimitive", IntegralConfig.builder().ruleRemark(MAX_VALUE).build());
        builder.setGeneratorConfig("longObject", IntegralConfig.builder().ruleRemark(MAX_VALUE).build());
        builder.setGeneratorConfig("longPrimitive", IntegralConfig.builder().ruleRemark(MIN_VALUE).build());
        builder.setGeneratorConfig("shortObject", IntegralConfig.builder().ruleRemark(MAX_VALUE).build());
        builder.setGeneratorConfig("shortPrimitive", IntegralConfig.builder().minValue((short) -100).build());

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
    void overrideGeneratorByField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator("intObject", () -> new Integer("1"))
                .setGenerator("intPrimitive", () -> 2)
                .setGenerator("longObject", () -> 3L)
                .setGenerator("longPrimitive", () -> new Long("4"))
                .setGenerator("shortObject", () -> new Short("5"))
                .setGenerator("shortPrimitive", () -> (short) 6)
                .setGenerator("byteObject", () -> (byte) 7)
                .setGenerator("bytePrimitive", () -> (byte) 8)
                .setGenerator("atomicInteger", () -> new AtomicInteger(9))
                .setGenerator("atomicLong", () -> new AtomicLong(99L))
                .setGenerator("bigInteger", () -> new BigInteger("10"));

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(1)),
                () -> assertThat(dto.intPrimitive, equalTo(2)),
                () -> assertThat(dto.longObject, equalTo(3L)),
                () -> assertThat(dto.longPrimitive, equalTo(4L)),
                () -> assertThat(dto.shortObject, equalTo((short) 5)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 6)),
                () -> assertThat(dto.byteObject, equalTo((byte) 7)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 8)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(9)),
                () -> assertThat(dto.atomicLong.get(), equalTo(99L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("10")))
        );

    }

    @Test
    void overrideGeneratorByType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator(Integer.class, () -> 1);
        builder.setGenerator(Long.class, () -> new Long("2"));
        builder.setGenerator(Short.class, () -> (short) 3);
        builder.setGenerator(Byte.class, () -> new Byte("4"));
        builder.setGenerator(BigInteger.class, () -> new BigInteger("5"));

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(1)),
                () -> assertThat(dto.intPrimitive, equalTo(1)),
                () -> assertThat(dto.longObject, equalTo(2L)),
                () -> assertThat(dto.longPrimitive, equalTo(2L)),
                () -> assertThat(dto.shortObject, equalTo((short) 3)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 3)),
                () -> assertThat(dto.byteObject, equalTo((byte) 4)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 4)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(1)),
                () -> assertThat(dto.atomicLong.get(), equalTo(2L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("5")))
        );

    }

    @Test
    void overrideGeneratorByTypeAndField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator(Integer.class, () -> 1)
                .setGenerator(Long.class, () -> new Long("2"))
                .setGenerator(Short.class, () -> (short) 3)
                .setGenerator(Byte.class, () -> new Byte("4"))
                .setGenerator(BigInteger.class, () -> new BigInteger("5"));

        builder.setGenerator("intPrimitive", () -> 11)
                .setGenerator("longObject", () -> 22L)
                .setGenerator("shortPrimitive", () -> (short) 33)
                .setGenerator("byteObject", () -> (byte) 44)
                .setGenerator("bigInteger", () -> new BigInteger("55"));


        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(1)),
                () -> assertThat(dto.intPrimitive, equalTo(11)),
                () -> assertThat(dto.longObject, equalTo(22L)),
                () -> assertThat(dto.longPrimitive, equalTo(2L)),
                () -> assertThat(dto.shortObject, equalTo((short) 3)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) 33)),
                () -> assertThat(dto.byteObject, equalTo((byte) 44)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 4)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(1)),
                () -> assertThat(dto.atomicLong.get(), equalTo(2L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("55")))
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

        AtomicInteger atomicInteger;
        AtomicLong atomicLong;
        BigInteger bigInteger;

    }

    @Test
    void withoutAnnotations() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);
        builder.getConfig().getTypeGeneratorsConfig().getIntegralConfig().setRuleRemark(MAX_VALUE);

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(Integer.MAX_VALUE)),
                () -> assertThat(dto.intPrimitive, equalTo(Integer.MAX_VALUE)),
                () -> assertThat(dto.longObject, equalTo(Long.MAX_VALUE)),
                () -> assertThat(dto.longPrimitive, equalTo(Long.MAX_VALUE)),
                () -> assertThat(dto.shortObject, equalTo(Short.MAX_VALUE)),
                () -> assertThat(dto.shortPrimitive, equalTo(Short.MAX_VALUE)),
                () -> assertThat(dto.byteObject, equalTo(Byte.MAX_VALUE)),
                () -> assertThat(dto.bytePrimitive, equalTo(Byte.MAX_VALUE)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(Integer.MAX_VALUE)),
                () -> assertThat(dto.atomicLong.get(), equalTo(Long.MAX_VALUE)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger(Bounds.BIG_INTEGER_MAX_VALUE)))

        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void withoutAnnotationsWithOverriddenConfig() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        // static
        builder.getStaticConfig().getTypeGeneratorsConfig().getIntegralConfig().setRuleRemark(MAX_VALUE);

        // instance
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);
        IntegralConfigCommonConfig numberConfig = builder.getConfig().getTypeGeneratorsConfig().getIntegralConfig();

        // next line overrides MAX_VALUE from static config
        numberConfig.setRuleRemark(MIN_VALUE)
                .setMinIntValue(1)
                .setMinLongValue(2L)
                .setMaxLongValue(22_222_222_222L)
                .setMinShortValue((short) 3)
                .setMinByteValue((byte) 4)
                .setMinBigIntValue("5");

        // next lines override parts of previous configs
        builder.setGeneratorConfig("intPrimitive", IntegralConfig.builder().minValue(-1).build())
                .setGeneratorConfig("longObject", IntegralConfig.builder().ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("shortPrimitive", IntegralConfig.builder().minValue((short) -3).build())
                .setGeneratorConfig("byteObject", IntegralConfig.builder().minValue(new Byte("-4")).build())
                .setGeneratorConfig("bigInteger", IntegralConfig.builder().minValue(new BigInteger("-5")).build());

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.intObject, equalTo(1)),
                () -> assertThat(dto.intPrimitive, equalTo(-1)),
                () -> assertThat(dto.longObject, equalTo(22_222_222_222L)),
                () -> assertThat(dto.longPrimitive, equalTo(2L)),
                () -> assertThat(dto.shortObject, equalTo((short) 3)),
                () -> assertThat(dto.shortPrimitive, equalTo((short) -3)),
                () -> assertThat(dto.byteObject, equalTo((byte) -4)),
                () -> assertThat(dto.bytePrimitive, equalTo((byte) 4)),
                () -> assertThat(dto.atomicInteger.get(), equalTo(1)),
                () -> assertThat(dto.atomicLong.get(), equalTo(2L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("-5")))
        );

    }

}
