package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.UtilsRoot;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigSupplier;
import org.laoruga.dtogenerator.functional.data.dto.DtoAllKnownTypes;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;
import org.laoruga.dtogenerator.generator.configs.datetime.ChronoUnitConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;
import static org.laoruga.dtogenerator.constants.Group.*;
import static org.laoruga.dtogenerator.constants.RuleRemark.MAX_VALUE;
import static org.laoruga.dtogenerator.constants.RuleRemark.MIN_VALUE;
import static org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType.ORG;
import static org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType.PERSON;

/**
 * @author Il'dar Valitov
 * Created on 02.07.2022
 */


@DisplayName("Overriding of generators")
@Epic("GENERATORS_OVERRIDING")
@ExtendWith(Extensions.RestoreStaticConfig.class)
@Slf4j
class OverridingOfGeneratorsTests {

    @Test
    @DisplayName("Overridden builders by field name. General known type generators")
    void fieldGeneratorOverridden() {

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class)
                .setGenerator("string", () -> "xxxxx")
                .setGenerator("integer", () -> 1)
                .setGenerator("aLong", () -> 2L)
                .setGenerator("aDouble", () -> 3D)
                .setGenerator("aBoolean", () -> true)
                .setGenerator("localDateTime", LocalDateTime::now)
                .setGenerator("clientType", () -> PERSON)
                .setGenerator("listOfString", () -> new LinkedList<>(Arrays.asList("yyy")))
                .setGenerator("setOfLong", () -> new HashSet<>(Arrays.asList(2L)))
                .setGenerator("linkedListOfEnum", () -> new LinkedList<>(Arrays.asList(ClientType.LEGAL_PERSON)))

                .setGenerator("innerDto.integer", () -> 123)
                .setGenerator("innerDto.string", () -> "fff")
                .setGenerator("innerDto.aLong", () -> 4L)
                .setGenerator("innerDto.aDouble", () -> 5D)
                .setGenerator("innerDto.localDateTime", () -> LocalDateTime.now().minusDays(1))
                .setGenerator("innerDto.clientType", () -> ORG)
                .setGenerator("innerDto.listOfDouble", () -> new CopyOnWriteArrayList<>(Arrays.asList(99.99D)))
                .setGenerator("innerDto.setOfInteger", () -> new LinkedHashSet<>(Arrays.asList(12345)))
                .setGenerator("innerDto.linkedListOfEnum", () -> new LinkedList<>(Arrays.asList(ClientType.ORG)));

        DtoAllKnownTypes dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        assertAll(
                () -> assertThat(dto.getString(), equalTo("xxxxx")),
                () -> assertThat(dto.getInteger(), equalTo(1)),
                () -> assertThat(dto.getALong(), equalTo(2L)),
                () -> assertThat(dto.getADouble(), equalTo(3D)),
                () -> assertThat(dto.getABoolean(), equalTo(true)),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getClientType(), equalTo(PERSON)),

                () -> assertThat(dto.getListOfString(), equalTo(new LinkedList<>(Arrays.asList("yyy")))),
                () -> assertThat(dto.getSetOfLong(), equalTo(new HashSet<>(Arrays.asList(2L)))),
                () -> assertThat(dto.getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.LEGAL_PERSON)))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getCustomInteger(), equalTo(888))
        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getInteger(), equalTo(123)),
                () -> assertThat(dto.getInnerDto().getString(), equalTo("fff")),
                () -> assertThat(dto.getInnerDto().getALong(), equalTo(4L)),
                () -> assertThat(dto.getInnerDto().getADouble(), equalTo(5D)),
                () -> assertThat(dto.getInnerDto().getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now().minusDays(1))),
                () -> assertThat(dto.getInnerDto().getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getInnerDto().getListOfDouble(), equalTo(new CopyOnWriteArrayList<>(Arrays.asList(99.99D)))),
                () -> assertThat(dto.getInnerDto().getSetOfInteger(), equalTo(new LinkedHashSet<>(Arrays.asList(12345)))),
                () -> assertThat(dto.getInnerDto().getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.ORG)))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getInnerDto().getCustomInteger(), equalTo(999))
        );

    }

    @Test
    @DisplayName("Overridden instance configs of known type generators")
    void overriddenInstanceConfig() {
        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class);
        TypeGeneratorsConfigSupplier gensConfig = builder.getConfig().getTypeGeneratorsConfig();

        gensConfig.getStringConfig()
                .setMinLength(1)
                .setMaxLength(100)
                .setRuleRemark(MIN_VALUE)
                .setChars("x");

        gensConfig.getNumberConfig()
                .setMinIntValue(-100)
                .setMaxIntValue(1)
                .setRuleRemark(MAX_VALUE);

        gensConfig.getDecimalConfig()
                .setMinDoubleValue(2D)
                .setMaxDoubleValue(100D)
                .setRuleRemarkDouble(MIN_VALUE);

        gensConfig.getNumberConfig()
                .setMinLongValue(-100L)
                .setMaxLongValue(3L)
                .setRuleRemark(MAX_VALUE);

        gensConfig.getDateTimeConfig(LocalDateTime.class)
                .addChronoConfig(
                        ChronoUnitConfig.newBounds(-1, 100, ChronoUnit.DAYS))
                .setRuleRemark(MIN_VALUE);

        gensConfig.getEnumConfig().setRuleRemark(MIN_VALUE);

        gensConfig.getCollectionConfig(List.class)
                .setCollectionInstanceSupplier(LinkedList::new)
                .setMinSize(0)
                .setMaxSize(1)
                .setRuleRemark(MAX_VALUE);

        DtoAllKnownTypes dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        assertOverriddenConfig(dto);
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    @DisplayName("Overridden static config of known type generators")
    void overriddenStaticConfig() {
        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class);
        TypeGeneratorsConfigSupplier gensConfig = builder.getStaticConfig().getTypeGeneratorsConfig();

        gensConfig.getStringConfig()
                .setMinLength(1)
                .setMaxLength(100)
                .setRuleRemark(MIN_VALUE)
                .setChars("x");

        gensConfig.getNumberConfig()
                .setMinIntValue(-100)
                .setMaxIntValue(1)
                .setRuleRemark(MAX_VALUE);

        gensConfig.getDecimalConfig()
                .setMinDoubleValue(2D)
                .setMaxDoubleValue(100D)
                .setRuleRemark(MIN_VALUE);

        gensConfig.getNumberConfig()
                .setMinLongValue(-100L)
                .setMaxLongValue(3L)
                .setRuleRemark(MAX_VALUE);

        gensConfig.getDateTimeConfig(LocalDateTime.class)
                .addChronoConfig(
                        ChronoUnitConfig.newBounds(-1, 100, ChronoUnit.DAYS))
                .setRuleRemark(MIN_VALUE);

        gensConfig.getEnumConfig().setRuleRemark(MIN_VALUE);

        gensConfig.getCollectionConfig(List.class)
                .setCollectionInstanceSupplier(LinkedList::new)
                .setMinSize(0)
                .setMaxSize(1)
                .setRuleRemark(MAX_VALUE);

        DtoAllKnownTypes dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        assertOverriddenConfig(dto);
    }

    public void assertOverriddenConfig(DtoAllKnownTypes dto) {
        assertAll(
                () -> assertThat(dto.getString(), equalTo("x")),
                () -> assertThat(dto.getInteger(), equalTo(1)),
                () -> assertThat(dto.getADouble(), equalTo(2D)),
                () -> assertThat(dto.getALong(), equalTo(3L)),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now().minusDays(1))),
                () -> assertThat(dto.getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getListOfString(), equalTo(new LinkedList<>(Arrays.asList("x")))),
                () -> assertThat(dto.getSetOfLong(), equalTo(new HashSet<>(Arrays.asList(3L)))),
                () -> assertThat(dto.getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.ORG)))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getCustomInteger(), equalTo(888))

        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getString(), equalTo("x")),
                () -> assertThat(dto.getInnerDto().getInteger(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getADouble(), equalTo(2D)),
                () -> assertThat(dto.getInnerDto().getALong(), equalTo(3L)),
                () -> assertThat(dto.getInnerDto().getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now().minusDays(1))),
                () -> assertThat(dto.getInnerDto().getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getInnerDto().getListOfDouble(), equalTo(new LinkedList<>(Arrays.asList(2D)))),
                () -> assertThat(dto.getInnerDto().getSetOfInteger(), equalTo(new HashSet<>(Arrays.asList(1)))),
                () -> assertThat(dto.getInnerDto().getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.ORG)))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getInnerDto().getCustomInteger(), equalTo(999))
        );
    }

    @Test
    @DisplayName("Overridden builders by annotations. Not known type generators")
    void overriddenBuildersByAnnotations() {

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class)
                .setGenerator(String.class, () -> "string")
                .setGenerator(Integer.class, () -> 1)
                .setGenerator(Long.class, () -> 2L)
                .setGenerator(Double.class, () -> 3D)
                .setGenerator(LocalDateTime.class, LocalDateTime::now)
                .setGenerator(Enum.class, () -> ClientType.ORG)
                .setGenerator(LinkedList.class, LinkedList::new)
                .setGenerator(List.class, LinkedList::new)
                .setGenerator(Set.class, HashSet::new);

        DtoAllKnownTypes dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        assertAll(
                () -> assertThat(dto.getString(), equalTo("string")),
                () -> assertThat(dto.getInteger(), equalTo(1)),
                () -> assertThat(dto.getALong(), equalTo(2L)),
                () -> assertThat(dto.getADouble(), equalTo(3D)),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getListOfString(), empty()),
                () -> assertThat(dto.getSetOfLong(), empty()),
                () -> assertThat(dto.getLinkedListOfEnum(), empty()),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getCustomInteger(), equalTo(1))
        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getString(), equalTo("string")),
                () -> assertThat(dto.getInnerDto().getInteger(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getALong(), equalTo(2L)),
                () -> assertThat(dto.getInnerDto().getADouble(), equalTo(3D)),
                () -> assertThat(dto.getInnerDto().getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getInnerDto().getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getInnerDto().getListOfDouble(), empty()),
                () -> assertThat(dto.getInnerDto().getSetOfInteger(), empty()),
                () -> assertThat(dto.getInnerDto().getLinkedListOfEnum(), empty()),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getInnerDto().getCustomInteger(), equalTo(1))
        );
    }

    @Getter
    @NoArgsConstructor
    static class DtoDifferent {

        @StringRule
        String stringWithEmptyRule;

        @StringRule(minLength = 3, maxLength = 3, ruleRemark = MIN_VALUE)
        String stringWithFullRule;

        String stringWithoutRule;

        @NumberRule(minInt = 2)
        Integer integerWithRule;

        @NumberRule
        Integer integerWithEmptyRule;

        @CollectionRule(element = @Entry(stringRule = @StringRule))
        List<String> listOfStringWithEmptyRule;

        @CollectionRule(
                minSize = 1, maxSize = 20,
                element = @Entry(stringRule = @StringRule))
        Set<String> setOfStringWithEmptyRule;
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    @DisplayName("Overridden builders and configs combinations")
    void overriddenBuildersAndConfigsCombination() {
        DtoGeneratorBuilder<DtoDifferent> builder = DtoGenerator.builder(DtoDifferent.class);
        builder.getStaticConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();
        TypeGeneratorsConfigSupplier userConfig = builder.getConfig().getTypeGeneratorsConfig();

        builder.setGenerator(String.class, () -> "o");

        userConfig.getStringConfig().setMinLength(2);
        staticConfig.getStringConfig().setMaxLength(2);

        builder.setGenerator("integerWithRule", () -> 2);

        userConfig.getCollectionConfig(Collection.class).setMaxSize(1);

        userConfig.getNumberConfig().setMinIntValue(0);
        staticConfig.getNumberConfig().setMaxIntValue(0);

        DtoDifferent dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        /*
         * static config for String max = 2
         * static config for Integer max = 0
         *
         * instance config for String min = 2
         * instance config for Collection max = 1
         */

        assertAll(
                () -> assertThat(dto.getStringWithEmptyRule(), equalTo("o")),
                () -> assertThat(dto.getStringWithFullRule(), equalTo("o")),
                () -> assertThat(dto.getStringWithoutRule(), equalTo("o")),

                () -> assertThat(dto.getIntegerWithRule(), equalTo(2)),
                () -> assertThat(dto.getIntegerWithEmptyRule(), equalTo(0)),

                () -> assertThat(dto.getListOfStringWithEmptyRule(), equalTo(Arrays.asList("o"))),
                () -> assertThat(dto.getSetOfStringWithEmptyRule(), equalTo(new HashSet<>(Arrays.asList("o"))))
        );

        DtoGeneratorBuilder<DtoDifferent> builder2 = DtoGenerator.builder(DtoDifferent.class);

        TypeGeneratorsConfigSupplier userConfig2 = builder2.getConfig().getTypeGeneratorsConfig();
        userConfig2.getNumberConfig().setMinIntValue(5);
        userConfig2.getNumberConfig().setMaxIntValue(5);
        userConfig2.getStringConfig().setChars("i");
        userConfig2.getCollectionConfig(Collection.class).setMaxSize(1);
        userConfig2.getCollectionConfig(Collection.class).setMinSize(1);
        builder2.setGenerator(List.class, ArrayList::new);

        DtoDifferent dto2 = builder2.build().generateDto();

        log.info(UtilsRoot.toJson(dto2));

        /*
         * static config did not change
         * static config for String max = 2
         * static config for Integer max = 0
         *
         * instance config for Integer min = max = 5
         * instance config for String chars = 'i'
         * instance config for Set min = max = 1
         *
         * builder of List with config min = max = 1
         */

        assertAll(
                // static max = 2; default min = 0; default remark = RANDOM
                () -> assertThat(dto2.getStringWithEmptyRule().length(),
                        both(greaterThanOrEqualTo(0))
                                .and(lessThanOrEqualTo(2))),

                // annotated min = 3; annotated max = 3; annotated remark = MIN
                () -> assertThat(dto2.getStringWithFullRule().length(), equalTo(3)),

                // default min = 0; static max = 2; default remark = RANDOM
                () -> assertThat(dto2.getStringWithoutRule(),
                        either(equalTo(""))
                                .or(equalTo("i"))
                                .or(equalTo("ii"))),

                // instance min = 5; instance max = 5;
                () -> assertThat(dto2.getIntegerWithRule(), equalTo(5)),
                () -> assertThat(dto2.getIntegerWithEmptyRule(), equalTo(5)),

                // String: static max = 2; default min = 0; default remark = RANDOM
                // List  : generator builder config min = max = 1
                () -> assertThat(dto2.getListOfStringWithEmptyRule().getClass(), equalTo(ArrayList.class)),
                () -> assertThat(dto2.getListOfStringWithEmptyRule().toString(),
                        either(equalTo(Arrays.asList("").toString()))
                                .or(equalTo(Arrays.asList("i").toString()))
                                .or(equalTo(Arrays.asList("ii").toString()))),

                // String: static max = 2; default min = 0; default remark = RANDOM
                // Set   : instance config min = max = 1
                () -> assertThat(dto2.getSetOfStringWithEmptyRule().getClass(), equalTo(HashSet.class)),
                () -> assertThat(dto2.getSetOfStringWithEmptyRule().toString(),
                        either(equalTo(Arrays.asList("").toString()))
                                .or(equalTo(Arrays.asList("i").toString()))
                                .or(equalTo(Arrays.asList("ii").toString())))

        );


    }

    @Getter
    static class DtoWithGroup {
        @StringRule(group = GROUP_1, minLength = 1, maxLength = 1)
        @StringRule(group = GROUP_2, minLength = 2, maxLength = 2)
        String string;

        @NumberRule
        Integer integer;

        @NumberRule(group = GROUP_1)
        Long aLong;

        @CollectionRule(
                minSize = 1, maxSize = 1,
                element = @Entry(stringRule = @StringRule))
        List<String> listOfString;

        @CollectionRule(
                group = GROUP_2, minSize = 2, maxSize = 2,
                element = @Entry(numberRule = @NumberRule(group = GROUP_2)))
        Set<Long> setOfLong;

        @DecimalRule(group = GROUP_2)
        double aDouble;
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    @DisplayName("With Grouping")
    void withGrouping() {
        TypeGeneratorsConfigSupplier gensConfig = DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig();
        gensConfig.getDecimalConfig().setMaxDoubleValue(1D);
        gensConfig.getDecimalConfig().setMinDoubleValue(1D);

        DtoWithGroup dto_1 = DtoGenerator.builder(DtoWithGroup.class)
                .includeGroups(DEFAULT, GROUP_2)
                .setGenerator(String.class, () -> "xxxxx")
                .setGenerator(Integer.class, () -> 5)
                .setGenerator("aLong", () -> 123L)
                .build().generateDto();

        DtoWithGroup dto_2 = DtoGenerator.builder(new DtoWithGroup())
                .setGenerator(String.class, () -> "xxxxx")
                .setGenerator(Integer.class, () -> 5)
                .build().generateDto();

        DtoWithGroup dto_3 = DtoGenerator.builder(DtoWithGroup.class)
                .includeGroups(GROUP_2)
                .setGenerator(Set.class, () -> new HashSet<>(Collections.singletonList("love")))
                .build().generateDto();

        DtoWithGroup dto_4 = DtoGenerator.builder(new DtoWithGroup())
                .includeGroups(DEFAULT)
                .setGenerator(List.class, () -> Arrays.asList("xxxx", "xxxx", "xxxx"))
                .build().generateDto();

        assertAll(
                // dto 1
                () -> assertThat(dto_1.getString(), equalTo("xxxxx")),
                () -> assertThat(dto_1.getInteger(), equalTo(5)),
                () -> assertThat(dto_1.getALong(), equalTo(123L)),
                () -> assertThat(dto_1.getListOfString().get(0), equalTo("xxxxx")),
                () -> assertThat(dto_1.getSetOfLong(), notNullValue()),
                () -> assertThat(dto_1.getADouble(), equalTo(1D)),

                // dto 2
                () -> assertThat(dto_2.getString(), nullValue()),
                () -> assertThat(dto_2.getInteger(), notNullValue()),
                () -> assertThat(dto_2.getALong(), nullValue()),
                () -> assertThat(dto_2.getListOfString().get(0), notNullValue()),
                () -> assertThat(dto_2.getSetOfLong(), nullValue()),
                () -> assertThat(dto_2.getADouble(), equalTo(0D)),

                // dto 3
                () -> assertThat(dto_3.getString().length(), equalTo(2)),
                () -> assertThat(dto_3.getInteger(), nullValue()),
                () -> assertThat(dto_3.getALong(), nullValue()),
                () -> assertThat(dto_3.getListOfString(), nullValue()),
                () -> assertThat(dto_3.getSetOfLong(), hasSize(1)),
                () -> assertThat(dto_3.getADouble(), equalTo(1D)),


                // dto 4
                () -> assertThat(dto_4.getString(), nullValue()),
                () -> assertThat(dto_4.getInteger(), notNullValue()),
                () -> assertThat(dto_4.getALong(), nullValue()),
                () -> assertThat(dto_4.getListOfString(), hasSize(3)),
                () -> assertThat(dto_4.getListOfString().get(1), equalTo("xxxx")),
                () -> assertThat(dto_4.getSetOfLong(), nullValue()),
                () -> assertThat(dto_4.getADouble(), equalTo(0D))

        );

    }

}
