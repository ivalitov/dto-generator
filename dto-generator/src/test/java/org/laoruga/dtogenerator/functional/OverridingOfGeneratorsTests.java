package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.UtilsRoot;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.TypeGeneratorBuildersConfig;
import org.laoruga.dtogenerator.functional.data.dto.DtoAllKnownTypes;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;
import org.laoruga.dtogenerator.generators.*;
import org.laoruga.dtogenerator.generators.builders.GeneratorBuildersFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.UtilsRoot.resetStaticConfig;
import static org.laoruga.dtogenerator.constants.RuleRemark.MAX_VALUE;
import static org.laoruga.dtogenerator.constants.RuleRemark.MIN_VALUE;

/**
 * @author Il'dar Valitov
 * Created on 02.07.2022
 */


//TODO need tests with grouping
@DisplayName("Overriding of generators")
@Epic("GENERATORS_OVERRIDING")
@Slf4j
class OverridingOfGeneratorsTests {

    @Test
    @DisplayName("Overridden builders by annotations. General known type generators")
    void
    basicGeneratorOverridden() {

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class)
                .setGeneratorBuilder(StringRule.class, GeneratorBuildersFactory.stringBuilder().minLength(5).maxLength(5).chars("x"))
                .setGeneratorBuilder(IntegerRule.class, GeneratorBuildersFactory.integerBuilder().minValue(1).maxValue(1))
                .setGeneratorBuilder(LongRule.class, GeneratorBuildersFactory.longBuilder().minValue(2L).maxValue(2L))
                .setGeneratorBuilder(DoubleRule.class, GeneratorBuildersFactory.doubleBuilder().minValue(3D).maxValue(3D))
                .setGeneratorBuilder(LocalDateTimeRule.class, GeneratorBuildersFactory.localDateTimeBuilder().leftShiftDays(0).rightShiftDays(0))
                .setGeneratorBuilder(EnumRule.class, GeneratorBuildersFactory.enumBuilder().possibleEnumNames("PERSON"))
                .setGeneratorBuilder(ListRule.class, GeneratorBuildersFactory.listBuilder()
                        .collectionInstance(LinkedList::new)
                        .minSize(1).maxSize(1))
                .setGeneratorBuilder(SetRule.class, GeneratorBuildersFactory.setBuilder()
                        .collectionInstance(HashSet::new)
                        .minSize(1).maxSize(1));

        DtoAllKnownTypes dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        assertAll(
                () -> assertThat(dto.getString(), equalTo("xxxxx")),
                () -> assertThat(dto.getInteger(), equalTo(1)),
                () -> assertThat(dto.getALong(), equalTo(2L)),
                () -> assertThat(dto.getADouble(), equalTo(3D)),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getClientType(), equalTo(ClientType.PERSON)),

                () -> assertThat(dto.getListOfString(), equalTo(new LinkedList<>(Arrays.asList("xxxxx")))),
                () -> assertThat(dto.getSetOfLong(), equalTo(new HashSet<>(Arrays.asList(2L)))),
                () -> assertThat(dto.getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.PERSON)))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getCustomInteger(), equalTo(888))
        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getString(), equalTo("xxxxx")),
                () -> assertThat(dto.getInnerDto().getInteger(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getALong(), equalTo(2L)),
                () -> assertThat(dto.getInnerDto().getADouble(), equalTo(3D)),
                () -> assertThat(dto.getInnerDto().getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getInnerDto().getClientType(), equalTo(ClientType.PERSON)),

                () -> assertThat(dto.getInnerDto().getListOfDouble(), equalTo(new LinkedList<>(Arrays.asList(3D)))),
                () -> assertThat(dto.getInnerDto().getSetOfInteger(), equalTo(new HashSet<>(Arrays.asList(1)))),
                () -> assertThat(dto.getInnerDto().getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.PERSON)))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getInnerDto().getCustomInteger(), equalTo(999))
        );
    }

    @Test
    @DisplayName("Overridden builders by field name. General known type generators")
    void fieldGeneratorOverridden() {

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class)
                .setGeneratorBuilder("string", GeneratorBuildersFactory.stringBuilder().minLength(5).maxLength(5).chars("x"))
                .setGeneratorBuilder("integer", GeneratorBuildersFactory.integerBuilder().minValue(1).maxValue(1))
                .setGeneratorBuilder("aLong", GeneratorBuildersFactory.longBuilder().minValue(2L).maxValue(2L))
                .setGeneratorBuilder("aDouble", GeneratorBuildersFactory.doubleBuilder().minValue(3D).maxValue(3D))
                .setGeneratorBuilder("localDateTime", GeneratorBuildersFactory.localDateTimeBuilder().leftShiftDays(0).rightShiftDays(0))
                .setGeneratorBuilder("clientType", GeneratorBuildersFactory.enumBuilder().possibleEnumNames("PERSON"))
                .setGeneratorBuilder("listOfString", GeneratorBuildersFactory.listBuilder()
                        .collectionInstance(LinkedList::new)
                        .elementGenerator(() -> "yyy")
                        .minSize(1).maxSize(1))
                .setGeneratorBuilder("setOfLong", GeneratorBuildersFactory.setBuilder()
                        .elementGenerator(() -> 2L)
                        .collectionInstance(HashSet::new)
                        .minSize(1).maxSize(1))
                .setGeneratorBuilder("linkedListOfEnum", GeneratorBuildersFactory.setBuilder()
                        .elementGenerator(() -> ClientType.LEGAL_PERSON)
                        .collectionInstance(LinkedList::new)
                        .minSize(1).maxSize(1))

                .setGeneratorBuilder("innerDto.integer", GeneratorBuildersFactory.integerBuilder().minValue(123).maxValue(123))
                .setGeneratorBuilder("innerDto.string", GeneratorBuildersFactory.stringBuilder().minLength(3).maxLength(3).chars("f"))
                .setGeneratorBuilder("innerDto.aLong", GeneratorBuildersFactory.longBuilder().minValue(4L).maxValue(4L))
                .setGeneratorBuilder("innerDto.aDouble", GeneratorBuildersFactory.doubleBuilder().minValue(5D).maxValue(5D))
                .setGeneratorBuilder("innerDto.localDateTime", GeneratorBuildersFactory.localDateTimeBuilder().leftShiftDays(1).rightShiftDays(-1))
                .setGeneratorBuilder("innerDto.clientType", GeneratorBuildersFactory.enumBuilder().possibleEnumNames("ORG"))
                .setGeneratorBuilder("innerDto.listOfDouble", GeneratorBuildersFactory.listBuilder()
                        .collectionInstance(CopyOnWriteArrayList::new)
                        .elementGenerator(() -> 99.99D)
                        .minSize(1).maxSize(1))
                .setGeneratorBuilder("innerDto.setOfInteger", GeneratorBuildersFactory.setBuilder()
                        .elementGenerator(() -> 12345)
                        .collectionInstance(LinkedHashSet::new)
                        .minSize(1).maxSize(1))
                .setGeneratorBuilder("innerDto.linkedListOfEnum", GeneratorBuildersFactory.setBuilder()
                        .elementGenerator(() -> ClientType.ORG)
                        .collectionInstance(LinkedList::new)
                        .minSize(1).maxSize(1));

        DtoAllKnownTypes dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        assertAll(
                () -> assertThat(dto.getString(), equalTo("xxxxx")),
                () -> assertThat(dto.getInteger(), equalTo(1)),
                () -> assertThat(dto.getALong(), equalTo(2L)),
                () -> assertThat(dto.getADouble(), equalTo(3D)),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dto.getClientType(), equalTo(ClientType.PERSON)),

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
        TypeGeneratorBuildersConfig gensConfig = builder.getUserConfig().getGenBuildersConfig();

        gensConfig.setConfig(
                StringGenerator.ConfigDto.builder()
                        .minLength(1)
                        .maxLength(100)
                        .ruleRemark(MIN_VALUE)
                        .chars("x").build());

        gensConfig.setConfig(
                IntegerGenerator.ConfigDto.builder()
                        .minValue(-100)
                        .maxValue(1)
                        .ruleRemark(MAX_VALUE)
                        .build());

        gensConfig.setConfig(
                DoubleGenerator.ConfigDto.builder()
                        .minValue(2D)
                        .maxValue(100D)
                        .ruleRemark(MIN_VALUE)
                        .build());

        gensConfig.setConfig(
                LongGenerator.ConfigDto.builder()
                        .minValue(-100L)
                        .maxValue(3L)
                        .ruleRemark(MAX_VALUE)
                        .build());

        gensConfig.setConfig(
                LocalDateTimeGenerator.ConfigDto.builder()
                        .leftShiftDays(-1)
                        .rightShiftDays(100)
                        .ruleRemark(MIN_VALUE)
                        .build());

        gensConfig.setConfig(
                EnumGenerator.ConfigDto.builder()
                        .ruleRemark(MIN_VALUE)
                        .build());

        gensConfig.setCollectionConfig(
                List.class,
                CollectionGenerator.ConfigDto.builder()
                        .minSize(2)
                        .maxSize(100)
                        .collectionInstance(LinkedList::new)
                        .ruleRemark(MIN_VALUE)
                        .build());

        gensConfig.setCollectionConfig(
                Set.class,
                CollectionGenerator.ConfigDto.builder()
                        .minSize(0)
                        .maxSize(1)
                        .ruleRemark(MAX_VALUE)
                        .build());

        DtoAllKnownTypes dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        assertOverriddenConfig(dto);
    }

    @Test
    @DisplayName("Overridden static config of known type generators")
    void overriddenStaticConfig() {
        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class);
        TypeGeneratorBuildersConfig gensConfig = DtoGeneratorStaticConfig.getInstance().getGenBuildersConfig();

        gensConfig.getStringConfig().setMinLength(1);
        gensConfig.getStringConfig().setMaxLength(100);
        gensConfig.getStringConfig().setRuleRemark(MIN_VALUE);
        gensConfig.getStringConfig().setChars("x");

        gensConfig.getIntegerConfig().setMinValue(-100);
        gensConfig.getIntegerConfig().setMaxValue(1);
        gensConfig.getIntegerConfig().setRuleRemark(MAX_VALUE);

        gensConfig.getDoubleConfig().setMinValue(2D);
        gensConfig.getDoubleConfig().setMaxValue(100D);
        gensConfig.getDoubleConfig().setRuleRemark(MIN_VALUE);

        gensConfig.getLongConfig().setMinValue(-100L);
        gensConfig.getLongConfig().setMaxValue(3L);
        gensConfig.getLongConfig().setRuleRemark(MAX_VALUE);

        gensConfig.getLocalDateTimeConfig().setLeftShiftDays(-1);
        gensConfig.getLocalDateTimeConfig().setRightShiftDays(100);
        gensConfig.getLocalDateTimeConfig().setRuleRemark(MIN_VALUE);

        gensConfig.getEnumConfig().setRuleRemark(MIN_VALUE);

        gensConfig.getListConfig().setMinSize(2);
        gensConfig.getListConfig().setMaxSize(100);
        gensConfig.getListConfig().setCollectionInstance(LinkedList::new);
        gensConfig.getListConfig().setRuleRemark(MIN_VALUE);

        gensConfig.getSetConfig().setMinSize(0);
        gensConfig.getSetConfig().setMaxSize(1);
        gensConfig.getSetConfig().setRuleRemark(MAX_VALUE);

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
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now().plusDays(1))),
                () -> assertThat(dto.getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getListOfString(), equalTo(new LinkedList<>(Arrays.asList("x", "x")))),
                () -> assertThat(dto.getSetOfLong(), equalTo(new HashSet<>(Arrays.asList(3L)))),
                () -> assertThat(dto.getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.ORG, ClientType.ORG)))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getCustomInteger(), equalTo(888))

        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getString(), equalTo("x")),
                () -> assertThat(dto.getInnerDto().getInteger(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getADouble(), equalTo(2D)),
                () -> assertThat(dto.getInnerDto().getALong(), equalTo(3L)),
                () -> assertThat(dto.getInnerDto().getLocalDateTimeAsIs().toLocalDate(), equalTo(LocalDate.now().plusDays(1))),
                () -> assertThat(dto.getInnerDto().getClientType(), equalTo(ClientType.ORG)),

                () -> assertThat(dto.getInnerDto().getListOfDouble(), equalTo(new LinkedList<>(Arrays.asList(2D, 2D)))),
                () -> assertThat(dto.getInnerDto().getSetOfInteger(), equalTo(new HashSet<>(Arrays.asList(1)))),
                () -> assertThat(dto.getInnerDto().getLinkedListOfEnum(), equalTo(new LinkedList<>(Arrays.asList(ClientType.ORG, ClientType.ORG)))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getInnerDto().getCustomInteger(), equalTo(999))
        );
    }

    @Test
    @DisplayName("Overridden builders by annotations. Not known type generators")
    void overriddenInstanceAndStaticConfig() {

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class)
                .setGeneratorBuilder(StringRule.class, () -> () -> "string")
                .setGeneratorBuilder(IntegerRule.class, () -> () -> 1)
                .setGeneratorBuilder(LongRule.class, () -> () -> 2L)
                .setGeneratorBuilder(DoubleRule.class, () -> () -> 3D)
                .setGeneratorBuilder(LocalDateTimeRule.class, () -> LocalDateTime::now)
                .setGeneratorBuilder(EnumRule.class, () -> () -> ClientType.ORG)
                .setGeneratorBuilder(ListRule.class, () -> LinkedList::new)
                .setGeneratorBuilder(SetRule.class, () -> HashSet::new);

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

                () -> assertThat(dto.getCustomInteger(), equalTo(888))
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

                () -> assertThat(dto.getInnerDto().getCustomInteger(), equalTo(999))
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

        @IntegerRule(minValue = 2)
        Integer integerWithRule;

        @IntegerRule
        Integer integerWithEmptyRule;

        @ListRule
        @StringRule
        List<String> listOfStringWithEmptyRule;

        @SetRule(minSize = 1, maxSize = 20)
        @StringRule
        Set<String> setOfStringWithEmptyRule;
    }

    // TODO This case needs full coverage
    @Test
    @DisplayName("Overridden builders and configs combinations")
    void overriddenBuildersAndConfigsCombination() {
        DtoGeneratorStaticConfig.getInstance().setGenerateAllKnownTypes(true);
        DtoGeneratorBuilder<DtoDifferent> builder = DtoGenerator.builder(DtoDifferent.class);

        TypeGeneratorBuildersConfig staticConfig = DtoGeneratorStaticConfig.getInstance().getGenBuildersConfig();
        TypeGeneratorBuildersConfig userConfig = builder.getUserConfig().getGenBuildersConfig();

        builder.setGeneratorBuilder(StringRule.class, GeneratorBuildersFactory.stringBuilder()
                .chars("o")
                .minLength(1)
                .maxLength(1)
        );
        userConfig.getStringConfig().setMinLength(2);
        staticConfig.getStringConfig().setMaxLength(2);

        builder.setGeneratorBuilder("integerWithRule", GeneratorBuildersFactory.integerBuilder()
                .maxValue(2)
                .ruleRemark(MAX_VALUE));

        userConfig.getSetConfig().setMaxSize(1);
        userConfig.getListConfig().setMaxSize(1);

        staticConfig.getIntegerConfig().setMaxValue(0);

        DtoDifferent dto = builder.build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        /*
         * static config for String max = 2
         * static config for Integer max = 0
         *
         * instance config for String min = 2
         * instance config for Set max = 1
         * instance config for List max = 1
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

        DtoGeneratorInstanceConfig userConfig2 = builder2.getUserConfig();
        userConfig2.getGenBuildersConfig().getIntegerConfig().setMinValue(5);
        userConfig2.getGenBuildersConfig().getIntegerConfig().setMaxValue(5);
        userConfig2.getGenBuildersConfig().getStringConfig().setChars("i");
        userConfig2.getGenBuildersConfig().getSetConfig().setMaxSize(1);
        userConfig2.getGenBuildersConfig().getSetConfig().setMinSize(1);
        builder2.setGeneratorBuilder(ListRule.class, GeneratorBuildersFactory.listBuilder().maxSize(1).minSize(1));

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

    @AfterEach
    @SneakyThrows
    void restoreConfig() {
        resetStaticConfig();
    }


}
