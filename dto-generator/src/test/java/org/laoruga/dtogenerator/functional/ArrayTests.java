package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.api.rules.Entry;
import org.laoruga.dtogenerator.api.rules.IntegralRule;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigSupplier;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;
import org.laoruga.dtogenerator.generator.config.dto.ArrayConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.Boundary.*;

/**
 * @author Il'dar Valitov
 * Created on 23.03.2023
 */
@ExtendWith(Extensions.RestoreStaticConfig.class)
@Epic("ARRAY_RULES")
class ArrayTests {

    static class Dto {

        @ArrayRule(minSize = 2)
        String[] strings;

        @ArrayRule(boundary = MIN_VALUE, maxSize = 4)
        Integer[] integers;

        @ArrayRule(boundary = Boundary.MAX_VALUE)
        int[] ints;

        @ArrayRule
        Long[] longsObjects;

        @ArrayRule(element = @Entry(numberRule =
        @IntegralRule(minLong = 1, maxLong = 10)))
        long[] longs;

        @ArrayRule
        ClientType[] enums;

    }


    @Test
    void annotationConfig() {

        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertAll(
                () -> assertThat(dto.strings.length, greaterThanOrEqualTo(2)),
                () -> assertThat(dto.integers.length, equalTo(1)),
                () -> assertThat(dto.ints.length, equalTo(10)),
                () -> assertThat(dto.longsObjects.length, greaterThanOrEqualTo(1)),
                () -> assertThat(dto.longs.length, greaterThanOrEqualTo(1)),
                () -> assertThat(dto.enums.length, greaterThanOrEqualTo(1))
        );
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();
        staticConfig.getArrayConfig(String[].class).setMinSize(1).setMaxSize(1);
        staticConfig.getArrayConfig(Integer[].class).setMinSize(2).setMaxSize(2);
        staticConfig.getArrayConfig(int[].class).setMinSize(3).setMaxSize(3);
        staticConfig.getArrayConfig(Long[].class).setMinSize(4).setMaxSize(4);
        staticConfig.getArrayConfig(long[].class).setMinSize(5).setMaxSize(5);
        staticConfig.getArrayConfig(ClientType[].class).setMinSize(6).setMaxSize(6);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.strings.length, equalTo(1)),
                () -> assertThat(dto.integers.length, equalTo(2)),
                () -> assertThat(dto.ints.length, equalTo(3)),
                () -> assertThat(dto.longsObjects.length, equalTo(4)),
                () -> assertThat(dto.longs.length, equalTo(5)),
                () -> assertThat(dto.enums.length, equalTo(6))
        );

    }

    @Test
    void instanceConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setBoundary(Boundary.MAX_VALUE);
        TypeGeneratorsConfigSupplier instance = builder.getConfig().getTypeGeneratorsConfig();
        instance.getArrayConfig(String[].class).setMinSize(0).setMaxSize(1);
        instance.getArrayConfig(Integer[].class).setMinSize(0).setMaxSize(2);
        instance.getArrayConfig(int[].class).setMinSize(0).setMaxSize(3);
        instance.getArrayConfig(Long[].class).setMinSize(0).setMaxSize(4);
        instance.getArrayConfig(long[].class).setMinSize(0).setMaxSize(5);
        instance.getArrayConfig(ClientType[].class).setMinSize(0).setMaxSize(6);

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.strings.length, equalTo(1)),
                () -> assertThat(dto.integers.length, equalTo(2)),
                () -> assertThat(dto.ints.length, equalTo(3)),
                () -> assertThat(dto.longsObjects.length, equalTo(4)),
                () -> assertThat(dto.longs.length, equalTo(5)),
                () -> assertThat(dto.enums.length, equalTo(6))
        );
    }

    @Test
    void fieldConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGeneratorConfig("strings",
                        ArrayConfig.builder()
                                .minSize(1)
                                .ruleRemark(MIN_VALUE)
                                .elementGenerator(() -> "OASIS")
                                .build())
                .setGeneratorConfig("integers",
                        ArrayConfig.builder()
                                .elementGenerator(() -> 2)
                                .ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("ints",
                        ArrayConfig.builder().minSize(6).maxSize(6).build())
                .setGeneratorConfig("longsObjects",
                        ArrayConfig.builder().minSize(8).maxSize(8).build())
                .setGeneratorConfig("longs",
                        ArrayConfig.builder()
                                .minSize(3)
                                .maxSize(3)
                                .elementGenerator(() -> 7L).build())
                .setGeneratorConfig("enums",
                        ArrayConfig.builder().maxSize(11).ruleRemark(MAX_VALUE).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.strings, equalTo(new String[]{"OASIS"})),
                () -> assertThat(dto.integers, equalTo(new Integer[]{2, 2, 2, 2})),
                () -> assertThat(dto.ints.length, equalTo(6)),
                () -> assertThat(dto.longsObjects.length, equalTo(8)),
                () -> assertThat(dto.longs, equalTo(new long[]{7, 7, 7})),
                () -> assertThat(dto.enums.length, equalTo(11))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void staticAndInstanceAndFieldAndAnnotationConfig() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        // static
        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();
        staticConfig.getArrayConfig(String[].class).setMaxSize(3);
        staticConfig.getArrayConfig(Integer[].class).setMinSize(1);
        staticConfig.getArrayConfig(Long[].class).setElementGenerator(() -> 9L).setMinSize(1).setMaxSize(1);
        staticConfig.getArrayConfig(long[].class).setMaxSize(5);
        staticConfig.getArrayConfig(ClientType[].class).setMinSize(6).setMaxSize(6);

        // instance
        TypeGeneratorsConfigSupplier instanceConfig = builder.getConfig().getTypeGeneratorsConfig();
        instanceConfig.getArrayConfig(String[].class).setRuleRemark(MAX_VALUE);
        instanceConfig.getArrayConfig(Integer[].class).setElementGenerator(() -> 3);
        instanceConfig.getArrayConfig(int[].class).setMinSize(1).setMaxSize(1);
        staticConfig.getArrayConfig(Long[].class).setElementGenerator(() -> 99L).setMinSize(2).setMaxSize(2);
        instanceConfig.getArrayConfig(long[].class).setRuleRemark(MIN_VALUE);
        instanceConfig.getArrayConfig(ClientType[].class).setMinSize(0).setMaxSize(0);

        // field
        builder.setGeneratorConfig("strings",
                        ArrayConfig.builder().elementGenerator(() -> "PUSHKIN").build())
                .setGeneratorConfig("integers",
                        ArrayConfig.builder().ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("ints",
                        ArrayConfig.builder().maxSize(6).ruleRemark(MIN_VALUE).build())
                .setGeneratorConfig("longs",
                        ArrayConfig.builder().elementGenerator(() -> 88L).build())
                .setGeneratorConfig("enums",
                        ArrayConfig.builder().ruleRemark(RANDOM_VALUE).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Static + instance + filed", dto.strings, equalTo(new String[]{"PUSHKIN", "PUSHKIN", "PUSHKIN"})),
                () -> assertThat("Annotation + instance + field", dto.integers, equalTo(new Integer[]{3, 3, 3, 3})),
                () -> assertThat("Instance + field", dto.ints.length, equalTo(1)),
                () -> assertThat("Static + instance", dto.longsObjects, equalTo(new Long[]{99L, 99L})),
                () -> assertThat("Annotation + instance + field", dto.longs, equalTo(new long[]{88L})),
                () -> assertThat("Instance + field", dto.enums.length, equalTo(0))
        );
    }

    @Test
    void overrideGeneratorByField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator("strings", () -> new String[]{"MOONLIGHT"})
                .setGenerator("integers", () -> new Integer[]{222})
                .setGenerator("ints", () -> new int[]{333})
                .setGenerator("longsObjects", () -> new Long[]{444L})
                .setGenerator("longs", () -> new long[]{555L})
                .setGenerator("enums", () -> new ClientType[]{ClientType.ORG});

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.strings, equalTo(new String[]{"MOONLIGHT"})),
                () -> assertThat(dto.integers, equalTo(new Integer[]{222})),
                () -> assertThat(dto.ints, equalTo(new int[]{333})),
                () -> assertThat(dto.longsObjects, equalTo(new Long[]{444L})),
                () -> assertThat(dto.longs, equalTo(new long[]{555L})),
                () -> assertThat(dto.enums, equalTo(new ClientType[]{ClientType.ORG}))
        );
    }

    @Test
    void overrideGeneratorByType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator(String[].class, () -> new String[]{"MOONLIGHT"})
                .setGenerator(Integer[].class, () -> new Integer[]{222})
                .setGenerator(int[].class, () -> new int[]{333})
                .setGenerator(Long[].class, () -> new Long[]{444L})
                .setGenerator(long[].class, () -> new long[]{555L})
                .setGenerator(ClientType[].class, () -> new ClientType[]{ClientType.ORG});

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.strings, equalTo(new String[]{"MOONLIGHT"})),
                () -> assertThat(dto.integers, equalTo(new Integer[]{222})),
                () -> assertThat(dto.ints, equalTo(new int[]{333})),
                () -> assertThat(dto.longsObjects, equalTo(new Long[]{444L})),
                () -> assertThat(dto.longs, equalTo(new long[]{555L})),
                () -> assertThat(dto.enums, equalTo(new ClientType[]{ClientType.ORG}))
        );
    }

    @Test
    void overrideGeneratorByTypeAndField() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setGenerator(String[].class, () -> new String[]{"MOONLIGHT"})
                .setGenerator(Integer[].class, () -> new Integer[]{222})
                .setGenerator(int[].class, () -> new int[]{333})
                .setGenerator(Long[].class, () -> new Long[]{444L})
                .setGenerator(long[].class, () -> new long[]{555L})
                .setGenerator(ClientType[].class, () -> new ClientType[]{ClientType.ORG});

        builder.setGenerator("strings", () -> new String[]{"SUNLIGHT"})
                .setGenerator("ints", () -> new int[]{33})
                .setGenerator("longsObjects", () -> new Long[]{44L})
                .setGenerator("enums", () -> new ClientType[]{ClientType.LEGAL_PERSON});

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.strings, equalTo(new String[]{"SUNLIGHT"})),
                () -> assertThat(dto.integers, equalTo(new Integer[]{222})),
                () -> assertThat(dto.ints, equalTo(new int[]{33})),
                () -> assertThat(dto.longsObjects, equalTo(new Long[]{44L})),
                () -> assertThat(dto.longs, equalTo(new long[]{555L})),
                () -> assertThat(dto.enums, equalTo(new ClientType[]{ClientType.LEGAL_PERSON}))
        );
    }

    static class Dto_2 {

        String[] strings;

        Integer[] integers;

        int[] ints;

        Long[] longsObjects;

        long[] longs;

        ClientType[] enums;
    }

    @Test
    void withoutAnnotations() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.strings.length, greaterThanOrEqualTo(1)),
                () -> assertThat(dto.integers.length, greaterThanOrEqualTo(1)),
                () -> assertThat(dto.ints.length, greaterThanOrEqualTo(1)),
                () -> assertThat(dto.longsObjects.length, greaterThanOrEqualTo(1)),
                () -> assertThat(dto.longs.length, greaterThanOrEqualTo(1)),
                () -> assertThat(dto.enums.length, greaterThanOrEqualTo(1))
        );

    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    void withoutAnnotationsWithOverriddenConfig() {

        DtoGeneratorBuilder<Dto_2> builder = DtoGenerator.builder(Dto_2.class);

        builder.getStaticConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        // static
        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();
        staticConfig.getArrayConfig(String[].class).setMaxSize(3);
        staticConfig.getArrayConfig(Integer[].class).setMinSize(1).setMaxSize(3);
        staticConfig.getArrayConfig(Long[].class).setElementGenerator(() -> 9L).setMinSize(1).setMaxSize(1);
        staticConfig.getArrayConfig(long[].class).setMaxSize(5);
        staticConfig.getArrayConfig(ClientType[].class).setMinSize(6).setMaxSize(6);

        // instance
        TypeGeneratorsConfigSupplier instanceConfig = builder.getConfig().getTypeGeneratorsConfig();
        instanceConfig.getArrayConfig(String[].class).setRuleRemark(MAX_VALUE);
        instanceConfig.getArrayConfig(Integer[].class).setElementGenerator(() -> 3);
        instanceConfig.getArrayConfig(int[].class).setMinSize(1).setMaxSize(1);
        staticConfig.getArrayConfig(Long[].class).setElementGenerator(() -> 99L).setMinSize(2).setMaxSize(2);
        instanceConfig.getArrayConfig(long[].class).setRuleRemark(MIN_VALUE);
        instanceConfig.getArrayConfig(ClientType[].class).setMinSize(0).setMaxSize(0);

        // field
        builder.setGeneratorConfig("strings",
                        ArrayConfig.builder().elementGenerator(() -> "PUSHKIN").build())
                .setGeneratorConfig("integers",
                        ArrayConfig.builder().ruleRemark(MAX_VALUE).build())
                .setGeneratorConfig("ints",
                        ArrayConfig.builder().maxSize(6).ruleRemark(MIN_VALUE).build())
                .setGeneratorConfig("longs",
                        ArrayConfig.builder().elementGenerator(() -> 88L).build())
                .setGeneratorConfig("enums",
                        ArrayConfig.builder().ruleRemark(RANDOM_VALUE).build());

        Dto_2 dto = builder.build().generateDto();

        assertAll(
                () -> assertThat("Static + instance + fied", dto.strings, equalTo(new String[]{"PUSHKIN", "PUSHKIN", "PUSHKIN"})),
                () -> assertThat("Default + instance + field", dto.integers, equalTo(new Integer[]{3, 3, 3})),
                () -> assertThat("Instance + field", dto.ints.length, equalTo(1)),
                () -> assertThat("Static + instance", dto.longsObjects, equalTo(new Long[]{99L, 99L})),
                () -> assertThat("Default + instance + field", dto.longs, equalTo(new long[]{88L})),
                () -> assertThat("Instance + field", dto.enums.length, equalTo(0))
        );
    }

}